package com.chubb.apac.claims.domain;

import com.chubb.apac.claims.common.enums.ClaimStatus;
import com.chubb.apac.claims.common.enums.Lob;
import com.chubb.apac.claims.common.enums.Market;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "claims", schema = "claims")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 32)
    private String claimNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Market market;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Lob lob;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ClaimStatus status;

    @Column(nullable = false, length = 64)
    private String claimantUserId;

    @Column(length = 64)
    private String adjusterUserId;

    @Column(nullable = false, length = 200)
    private String incidentSummary;

    @Column(nullable = false, length = 4000)
    private String incidentDescription;

    @Column(nullable = false)
    private LocalDate incidentDate;

    @Column(nullable = false, length = 200)
    private String location;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal estimatedLoss;

    @Column(precision = 14, scale = 2)
    private BigDecimal reserveAmount;

    @Column(precision = 14, scale = 2)
    private BigDecimal settlementAmount;

    @Column(length = 2000)
    private String infoRequestMessage;

    @Column(length = 4000)
    private String infoResponseMessage;

    @Column(length = 2000)
    private String decisionReason;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = ClaimStatus.SUBMITTED;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getClaimNumber() {
        return claimNumber;
    }

    public void setClaimNumber(String claimNumber) {
        this.claimNumber = claimNumber;
    }

    public Market getMarket() {
        return market;
    }

    public void setMarket(Market market) {
        this.market = market;
    }

    public Lob getLob() {
        return lob;
    }

    public void setLob(Lob lob) {
        this.lob = lob;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public String getClaimantUserId() {
        return claimantUserId;
    }

    public void setClaimantUserId(String claimantUserId) {
        this.claimantUserId = claimantUserId;
    }

    public String getAdjusterUserId() {
        return adjusterUserId;
    }

    public void setAdjusterUserId(String adjusterUserId) {
        this.adjusterUserId = adjusterUserId;
    }

    public String getIncidentSummary() {
        return incidentSummary;
    }

    public void setIncidentSummary(String incidentSummary) {
        this.incidentSummary = incidentSummary;
    }

    public String getIncidentDescription() {
        return incidentDescription;
    }

    public void setIncidentDescription(String incidentDescription) {
        this.incidentDescription = incidentDescription;
    }

    public LocalDate getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(LocalDate incidentDate) {
        this.incidentDate = incidentDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getEstimatedLoss() {
        return estimatedLoss;
    }

    public void setEstimatedLoss(BigDecimal estimatedLoss) {
        this.estimatedLoss = estimatedLoss;
    }

    public BigDecimal getReserveAmount() {
        return reserveAmount;
    }

    public void setReserveAmount(BigDecimal reserveAmount) {
        this.reserveAmount = reserveAmount;
    }

    public BigDecimal getSettlementAmount() {
        return settlementAmount;
    }

    public void setSettlementAmount(BigDecimal settlementAmount) {
        this.settlementAmount = settlementAmount;
    }

    public String getInfoRequestMessage() {
        return infoRequestMessage;
    }

    public void setInfoRequestMessage(String infoRequestMessage) {
        this.infoRequestMessage = infoRequestMessage;
    }

    public String getInfoResponseMessage() {
        return infoResponseMessage;
    }

    public void setInfoResponseMessage(String infoResponseMessage) {
        this.infoResponseMessage = infoResponseMessage;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public void setDecisionReason(String decisionReason) {
        this.decisionReason = decisionReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
