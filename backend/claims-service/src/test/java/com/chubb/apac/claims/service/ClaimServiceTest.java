package com.chubb.apac.claims.service;

import com.chubb.apac.claims.common.enums.ClaimStatus;
import com.chubb.apac.claims.common.enums.DecisionOutcome;
import com.chubb.apac.claims.common.enums.Lob;
import com.chubb.apac.claims.common.enums.Market;
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
import com.chubb.apac.claims.workload.WorkloadCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    ClaimRepository claimRepository;

    @Mock
    ClaimEventPublisher eventPublisher;

    @Mock
    WorkloadCacheService workloadCacheService;

    @InjectMocks
    ClaimService claimService;

    AuthenticatedUser claimant;
    AuthenticatedUser adjuster;

    @BeforeEach
    void setUp() {
        claimant = new AuthenticatedUser("u-claimant-1", "claimant1", "Aisha Tan", Role.CLAIMANT);
        adjuster = new AuthenticatedUser("u-adjuster-1", "adjuster1", "Ben Lee", Role.ADJUSTER);
        lenient().when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> {
            Claim claim = invocation.getArgument(0);
            if (claim.getId() == null) {
                claim.setId(UUID.randomUUID());
            }
            Instant now = Instant.now();
            ReflectionTestUtils.setField(claim, "createdAt", now);
            ReflectionTestUtils.setField(claim, "updatedAt", now);
            return claim;
        });
    }

    @Test
    void createSeedsReserveFromEstimatedLossAndPublishesSubmitted() {
        CreateClaimRequest request = new CreateClaimRequest(
                Market.SG,
                Lob.MOTOR,
                "Rear-end collision",
                "Hit at traffic light",
                LocalDate.of(2026, 11, 1),
                "Orchard Road",
                new BigDecimal("2500.00")
        );

        ClaimResponse response = claimService.create(request, claimant);

        assertThat(response.status()).isEqualTo(ClaimStatus.SUBMITTED);
        assertThat(response.reserveAmount()).isEqualByComparingTo("2500.00");
        assertThat(response.claimantUserId()).isEqualTo("u-claimant-1");
        assertThat(response.claimNumber()).startsWith("CLM-SG-");
        verify(workloadCacheService).onClaimChanged(any(), any());

        ArgumentCaptor<ClaimDomainEvent> eventCaptor = ArgumentCaptor.forClass(ClaimDomainEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().eventType()).isEqualTo(ClaimEventType.SUBMITTED);
    }

    @Test
    void createRejectsNonClaimant() {
        CreateClaimRequest request = new CreateClaimRequest(
                Market.SG,
                Lob.MOTOR,
                "Summary",
                "Description",
                LocalDate.of(2026, 11, 1),
                "Location",
                new BigDecimal("100")
        );

        assertThatThrownBy(() -> claimService.create(request, adjuster))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    @Test
    void assignMovesSubmittedClaimToAssigned() {
        Claim claim = openClaim(ClaimStatus.SUBMITTED, null);
        when(claimRepository.findById(claim.getId())).thenReturn(Optional.of(claim));

        ClaimResponse response = claimService.assign(claim.getId(), adjuster);

        assertThat(response.status()).isEqualTo(ClaimStatus.ASSIGNED);
        assertThat(response.adjusterUserId()).isEqualTo("u-adjuster-1");
        ArgumentCaptor<ClaimDomainEvent> eventCaptor = ArgumentCaptor.forClass(ClaimDomainEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().eventType()).isEqualTo(ClaimEventType.ASSIGNED);
    }

    @Test
    void requestInfoAndProvideInfoRoundTrip() {
        Claim claim = openClaim(ClaimStatus.ASSIGNED, "u-adjuster-1");
        when(claimRepository.findById(claim.getId())).thenReturn(Optional.of(claim));

        ClaimResponse requested = claimService.requestInfo(
                claim.getId(),
                new InfoMessageRequest("Please send photos"),
                adjuster
        );
        assertThat(requested.status()).isEqualTo(ClaimStatus.INFO_REQUESTED);
        assertThat(requested.infoRequestMessage()).isEqualTo("Please send photos");

        ClaimResponse provided = claimService.provideInfo(
                claim.getId(),
                new InfoMessageRequest("Photos attached"),
                claimant
        );
        assertThat(provided.status()).isEqualTo(ClaimStatus.UNDER_REVIEW);
        assertThat(provided.infoResponseMessage()).isEqualTo("Photos attached");
    }

    @Test
    void decideSettlesClaimAndClearsReserve() {
        Claim claim = openClaim(ClaimStatus.UNDER_REVIEW, "u-adjuster-1");
        when(claimRepository.findById(claim.getId())).thenReturn(Optional.of(claim));

        ClaimResponse response = claimService.decide(
                claim.getId(),
                new DecideClaimRequest(DecisionOutcome.SETTLED, "Covered under policy", new BigDecimal("2000"), null),
                adjuster
        );

        assertThat(response.status()).isEqualTo(ClaimStatus.SETTLED);
        assertThat(response.settlementAmount()).isEqualByComparingTo("2000");
        assertThat(response.reserveAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        ArgumentCaptor<ClaimDomainEvent> eventCaptor = ArgumentCaptor.forClass(ClaimDomainEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().eventType()).isEqualTo(ClaimEventType.DECIDED);
    }

    @Test
    void listForClaimantReturnsOnlyOwnClaims() {
        Claim mine = openClaim(ClaimStatus.SUBMITTED, null);
        when(claimRepository.findByClaimantUserIdOrderByCreatedAtDesc("u-claimant-1"))
                .thenReturn(List.of(mine));

        List<ClaimResponse> responses = claimService.list(claimant);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().claimantUserId()).isEqualTo("u-claimant-1");
    }

    private Claim openClaim(ClaimStatus status, String adjusterUserId) {
        Claim claim = new Claim();
        claim.setId(UUID.randomUUID());
        claim.setClaimNumber("CLM-SG-2026-2001");
        claim.setMarket(Market.SG);
        claim.setLob(Lob.MOTOR);
        claim.setStatus(status);
        claim.setClaimantUserId("u-claimant-1");
        claim.setAdjusterUserId(adjusterUserId);
        claim.setIncidentSummary("Rear-end collision");
        claim.setIncidentDescription("Hit at traffic light");
        claim.setIncidentDate(LocalDate.of(2026, 11, 1));
        claim.setLocation("Orchard Road");
        claim.setEstimatedLoss(new BigDecimal("2500.00"));
        claim.setReserveAmount(new BigDecimal("2500.00"));
        Instant now = Instant.now();
        ReflectionTestUtils.setField(claim, "createdAt", now);
        ReflectionTestUtils.setField(claim, "updatedAt", now);
        return claim;
    }
}
