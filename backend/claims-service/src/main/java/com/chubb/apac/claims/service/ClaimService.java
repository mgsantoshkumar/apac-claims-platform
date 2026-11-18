package com.chubb.apac.claims.service;

import com.chubb.apac.claims.common.enums.ClaimStatus;
import com.chubb.apac.claims.common.enums.DecisionOutcome;
import com.chubb.apac.claims.common.enums.Role;
import com.chubb.apac.claims.common.events.ClaimDomainEvent;
import com.chubb.apac.claims.common.events.ClaimEventType;
import com.chubb.apac.claims.domain.Claim;
import com.chubb.apac.claims.messaging.ClaimEventPublisher;
import com.chubb.apac.claims.repository.ClaimRepository;
import com.chubb.apac.claims.security.AuthenticatedUser;
import com.chubb.apac.claims.web.dto.ClaimResponse;
import com.chubb.apac.claims.web.dto.CreateClaimRequest;
import com.chubb.apac.claims.web.dto.DecideClaimRequest;
import com.chubb.apac.claims.web.dto.InfoMessageRequest;
import com.chubb.apac.claims.web.dto.WorkloadSummaryResponse;
import com.chubb.apac.claims.workload.WorkloadCacheService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ClaimEventPublisher eventPublisher;
    private final WorkloadCacheService workloadCacheService;
    private final AtomicInteger sequence = new AtomicInteger(1000);

    public ClaimService(
            ClaimRepository claimRepository,
            ClaimEventPublisher eventPublisher,
            WorkloadCacheService workloadCacheService
    ) {
        this.claimRepository = claimRepository;
        this.eventPublisher = eventPublisher;
        this.workloadCacheService = workloadCacheService;
    }

    @Transactional
    public ClaimResponse create(CreateClaimRequest request, AuthenticatedUser user) {
        requireRole(user, Role.CLAIMANT);
        Claim claim = new Claim();
        claim.setClaimNumber(nextClaimNumber(request.market().name()));
        claim.setMarket(request.market());
        claim.setLob(request.lob());
        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setClaimantUserId(user.userId());
        claim.setIncidentSummary(request.incidentSummary());
        claim.setIncidentDescription(request.incidentDescription());
        claim.setIncidentDate(request.incidentDate());
        claim.setLocation(request.location());
        claim.setEstimatedLoss(request.estimatedLoss());
        claim.setReserveAmount(request.estimatedLoss());
        Claim saved = claimRepository.save(claim);
        workloadCacheService.onClaimChanged(null, saved);
        publish(ClaimEventType.SUBMITTED, saved, user.userId(), "Claim submitted");
        return ClaimResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> list(AuthenticatedUser user) {
        List<Claim> claims = switch (user.role()) {
            case CLAIMANT -> claimRepository.findByClaimantUserIdOrderByCreatedAtDesc(user.userId());
            case ADJUSTER -> {
                List<Claim> mine = claimRepository.findByAdjusterUserIdOrderByUpdatedAtDesc(user.userId());
                List<Claim> inbox = claimRepository.findByStatusOrderByCreatedAtAsc(ClaimStatus.SUBMITTED);
                inbox.addAll(mine.stream().filter(c -> c.getStatus() != ClaimStatus.SUBMITTED).toList());
                yield inbox.stream().distinct().toList();
            }
            case MANAGER -> claimRepository.findAllByOrderByUpdatedAtDesc();
        };
        return claims.stream().map(ClaimResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ClaimResponse get(UUID id, AuthenticatedUser user) {
        Claim claim = findVisible(id, user);
        return ClaimResponse.from(claim);
    }

    @Transactional
    public ClaimResponse assign(UUID id, AuthenticatedUser user) {
        requireRole(user, Role.ADJUSTER, Role.MANAGER);
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Claim not found"));
        if (claim.getStatus() != ClaimStatus.SUBMITTED && claim.getAdjusterUserId() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Claim already assigned");
        }
        claim.setAdjusterUserId(user.userId());
        claim.setStatus(ClaimStatus.ASSIGNED);
        Claim saved = claimRepository.save(claim);
        workloadCacheService.onClaimChanged(null, saved);
        publish(ClaimEventType.ASSIGNED, saved, user.userId(), "Claim assigned to adjuster");
        return ClaimResponse.from(saved);
    }

    @Transactional
    public ClaimResponse requestInfo(UUID id, InfoMessageRequest request, AuthenticatedUser user) {
        requireRole(user, Role.ADJUSTER, Role.MANAGER);
        Claim claim = findAssignedToStaff(id, user);
        claim.setInfoRequestMessage(request.message());
        claim.setStatus(ClaimStatus.INFO_REQUESTED);
        Claim saved = claimRepository.save(claim);
        workloadCacheService.onClaimChanged(null, saved);
        publish(ClaimEventType.INFO_REQUESTED, saved, user.userId(), request.message());
        return ClaimResponse.from(saved);
    }

    @Transactional
    public ClaimResponse provideInfo(UUID id, InfoMessageRequest request, AuthenticatedUser user) {
        requireRole(user, Role.CLAIMANT);
        Claim claim = findVisible(id, user);
        if (claim.getStatus() != ClaimStatus.INFO_REQUESTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No information has been requested");
        }
        claim.setInfoResponseMessage(request.message());
        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        Claim saved = claimRepository.save(claim);
        workloadCacheService.onClaimChanged(null, saved);
        publish(ClaimEventType.INFO_PROVIDED, saved, user.userId(), request.message());
        return ClaimResponse.from(saved);
    }

    @Transactional
    public ClaimResponse decide(UUID id, DecideClaimRequest request, AuthenticatedUser user) {
        requireRole(user, Role.ADJUSTER, Role.MANAGER);
        Claim claim = findAssignedToStaff(id, user);
        if (claim.getStatus() == ClaimStatus.SETTLED || claim.getStatus() == ClaimStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Claim already decided");
        }
        if (request.outcome() == DecisionOutcome.SETTLED) {
            BigDecimal amount = request.settlementAmount() != null ? request.settlementAmount() : claim.getEstimatedLoss();
            claim.setSettlementAmount(amount);
            claim.setReserveAmount(BigDecimal.ZERO);
            claim.setStatus(ClaimStatus.SETTLED);
        } else {
            claim.setSettlementAmount(BigDecimal.ZERO);
            claim.setReserveAmount(BigDecimal.ZERO);
            claim.setStatus(ClaimStatus.REJECTED);
        }
        claim.setDecisionReason(request.reason());
        if (request.reserveAmount() != null && claim.getStatus() != ClaimStatus.SETTLED
                && claim.getStatus() != ClaimStatus.REJECTED) {
            claim.setReserveAmount(request.reserveAmount());
        }
        Claim saved = claimRepository.save(claim);
        workloadCacheService.onClaimChanged(null, saved);
        publish(ClaimEventType.DECIDED, saved, user.userId(), request.reason());
        return ClaimResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public WorkloadSummaryResponse workload(AuthenticatedUser user) {
        requireRole(user, Role.ADJUSTER, Role.MANAGER);
        long mine = claimRepository.findByAdjusterUserIdOrderByUpdatedAtDesc(user.userId()).stream()
                .filter(c -> c.getStatus() != ClaimStatus.SETTLED && c.getStatus() != ClaimStatus.REJECTED)
                .count();
        return new WorkloadSummaryResponse(
                workloadCacheService.statusCounts(),
                workloadCacheService.marketCounts(),
                workloadCacheService.openReserve(),
                workloadCacheService.unassignedCount(),
                mine
        );
    }

    private Claim findVisible(UUID id, AuthenticatedUser user) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Claim not found"));
        if (user.role() == Role.CLAIMANT && !claim.getClaimantUserId().equals(user.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your claim");
        }
        return claim;
    }

    private Claim findAssignedToStaff(UUID id, AuthenticatedUser user) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Claim not found"));
        if (user.role() == Role.ADJUSTER
                && claim.getAdjusterUserId() != null
                && !claim.getAdjusterUserId().equals(user.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Claim assigned to another adjuster");
        }
        return claim;
    }

    private void publish(ClaimEventType type, Claim claim, String actorUserId, String message) {
        eventPublisher.publish(ClaimDomainEvent.of(
                type,
                claim.getId(),
                claim.getClaimNumber(),
                claim.getStatus(),
                claim.getMarket(),
                claim.getLob(),
                claim.getClaimantUserId(),
                claim.getAdjusterUserId(),
                actorUserId,
                message,
                claim.getReserveAmount(),
                claim.getSettlementAmount()
        ));
    }

    private String nextClaimNumber(String market) {
        return "CLM-" + market + "-" + Year.now().getValue() + "-" + sequence.incrementAndGet();
    }

    private void requireRole(AuthenticatedUser user, Role... roles) {
        for (Role role : roles) {
            if (user.role() == role) {
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient role");
    }
}
