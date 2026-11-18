package com.chubb.apac.claims.web;

import com.chubb.apac.claims.security.AuthenticatedUser;
import com.chubb.apac.claims.security.SecurityUtils;
import com.chubb.apac.claims.service.ClaimService;
import com.chubb.apac.claims.web.dto.ClaimResponse;
import com.chubb.apac.claims.web.dto.CreateClaimRequest;
import com.chubb.apac.claims.web.dto.DecideClaimRequest;
import com.chubb.apac.claims.web.dto.InfoMessageRequest;
import com.chubb.apac.claims.web.dto.WorkloadSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    public ClaimResponse create(@Valid @RequestBody CreateClaimRequest request) {
        return claimService.create(request, SecurityUtils.currentUser());
    }

    @GetMapping
    public List<ClaimResponse> list() {
        return claimService.list(SecurityUtils.currentUser());
    }

    @GetMapping("/{id}")
    public ClaimResponse get(@PathVariable UUID id) {
        return claimService.get(id, SecurityUtils.currentUser());
    }

    @PostMapping("/{id}/assign")
    public ClaimResponse assign(@PathVariable UUID id) {
        return claimService.assign(id, SecurityUtils.currentUser());
    }

    @PostMapping("/{id}/request-info")
    public ClaimResponse requestInfo(@PathVariable UUID id, @Valid @RequestBody InfoMessageRequest request) {
        return claimService.requestInfo(id, request, SecurityUtils.currentUser());
    }

    @PostMapping("/{id}/provide-info")
    public ClaimResponse provideInfo(@PathVariable UUID id, @Valid @RequestBody InfoMessageRequest request) {
        return claimService.provideInfo(id, request, SecurityUtils.currentUser());
    }

    @PostMapping("/{id}/decide")
    public ClaimResponse decide(@PathVariable UUID id, @Valid @RequestBody DecideClaimRequest request) {
        return claimService.decide(id, request, SecurityUtils.currentUser());
    }

    @GetMapping("/workload/summary")
    public WorkloadSummaryResponse workload() {
        AuthenticatedUser user = SecurityUtils.currentUser();
        return claimService.workload(user);
    }
}
