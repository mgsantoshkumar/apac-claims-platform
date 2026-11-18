package com.chubb.apac.claims.config;

import com.chubb.apac.claims.common.enums.ClaimStatus;
import com.chubb.apac.claims.common.enums.Lob;
import com.chubb.apac.claims.common.enums.Market;
import com.chubb.apac.claims.common.enums.Role;
import com.chubb.apac.claims.domain.Claim;
import com.chubb.apac.claims.domain.UserAccount;
import com.chubb.apac.claims.repository.ClaimRepository;
import com.chubb.apac.claims.repository.UserAccountRepository;
import com.chubb.apac.claims.workload.WorkloadCacheService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserAccountRepository userAccountRepository;
    private final ClaimRepository claimRepository;
    private final PasswordEncoder passwordEncoder;
    private final WorkloadCacheService workloadCacheService;

    public DataSeeder(
            UserAccountRepository userAccountRepository,
            ClaimRepository claimRepository,
            PasswordEncoder passwordEncoder,
            WorkloadCacheService workloadCacheService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.claimRepository = claimRepository;
        this.passwordEncoder = passwordEncoder;
        this.workloadCacheService = workloadCacheService;
    }

    @Override
    public void run(String... args) {
        if (userAccountRepository.count() == 0) {
            userAccountRepository.save(user("u-claimant-1", "claimant1", "Aisha Tan", Role.CLAIMANT, null));
            userAccountRepository.save(user("u-claimant-2", "claimant2", "Wei Chen", Role.CLAIMANT, null));
            userAccountRepository.save(user("u-adjuster-1", "adjuster1", "Priya Nair", Role.ADJUSTER, "APAC Motor"));
            userAccountRepository.save(user("u-adjuster-2", "adjuster2", "James Ong", Role.ADJUSTER, "APAC Property"));
            userAccountRepository.save(user("u-manager-1", "manager1", "Mei Lin", Role.MANAGER, "APAC Claims"));
        }

        if (claimRepository.count() == 0) {
            Claim openMotor = baseClaim("CLM-SG-2026-1001", Market.SG, Lob.MOTOR, "u-claimant-1",
                    "Rear-end collision on CTE", "Vehicle hit from behind during peak hour congestion.",
                    "CTE Singapore", new BigDecimal("4500.00"));
            openMotor.setStatus(ClaimStatus.SUBMITTED);
            claimRepository.save(openMotor);

            Claim property = baseClaim("CLM-HK-2026-1002", Market.HK, Lob.PROPERTY, "u-claimant-2",
                    "Water damage from upstairs unit", "Ceiling leak damaged living room flooring and furniture.",
                    "Kowloon, Hong Kong", new BigDecimal("12000.00"));
            property.setStatus(ClaimStatus.ASSIGNED);
            property.setAdjusterUserId("u-adjuster-2");
            claimRepository.save(property);
        }

        workloadCacheService.rebuild();
    }

    private UserAccount user(String id, String username, String displayName, Role role, String team) {
        UserAccount account = new UserAccount();
        account.setId(id);
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode("password"));
        account.setDisplayName(displayName);
        account.setRole(role);
        account.setTeam(team);
        return account;
    }

    private Claim baseClaim(
            String number,
            Market market,
            Lob lob,
            String claimantId,
            String summary,
            String description,
            String location,
            BigDecimal loss
    ) {
        Claim claim = new Claim();
        claim.setClaimNumber(number);
        claim.setMarket(market);
        claim.setLob(lob);
        claim.setClaimantUserId(claimantId);
        claim.setIncidentSummary(summary);
        claim.setIncidentDescription(description);
        claim.setIncidentDate(LocalDate.now().minusDays(2));
        claim.setLocation(location);
        claim.setEstimatedLoss(loss);
        claim.setReserveAmount(loss);
        return claim;
    }
}
