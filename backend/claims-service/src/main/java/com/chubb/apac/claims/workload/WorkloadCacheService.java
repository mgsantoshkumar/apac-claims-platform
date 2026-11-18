package com.chubb.apac.claims.workload;

import com.chubb.apac.claims.common.enums.ClaimStatus;
import com.chubb.apac.claims.common.enums.Market;
import com.chubb.apac.claims.domain.Claim;
import com.chubb.apac.claims.repository.ClaimRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Service
public class WorkloadCacheService {

    public static final String KEY_STATUS = "claims:workload:status";
    public static final String KEY_MARKET = "claims:workload:market";
    public static final String KEY_RESERVE = "claims:workload:openReserve";
    public static final String KEY_UNASSIGNED = "claims:workload:unassigned";

    private final StringRedisTemplate redis;
    private final ClaimRepository claimRepository;

    public WorkloadCacheService(StringRedisTemplate redis, ClaimRepository claimRepository) {
        this.redis = redis;
        this.claimRepository = claimRepository;
    }

    public void rebuild() {
        redis.delete(KEY_STATUS);
        redis.delete(KEY_MARKET);
        redis.delete(KEY_RESERVE);
        redis.delete(KEY_UNASSIGNED);

        Map<ClaimStatus, Long> byStatus = new EnumMap<>(ClaimStatus.class);
        for (Object[] row : claimRepository.countByStatus()) {
            byStatus.put((ClaimStatus) row[0], (Long) row[1]);
        }
        byStatus.forEach((status, count) -> redis.opsForHash().put(KEY_STATUS, status.name(), String.valueOf(count)));

        for (Object[] row : claimRepository.countByMarket()) {
            redis.opsForHash().put(KEY_MARKET, ((Market) row[0]).name(), String.valueOf(row[1]));
        }

        BigDecimal reserve = claimRepository.sumOpenReserves();
        redis.opsForValue().set(KEY_RESERVE, reserve == null ? "0" : reserve.toPlainString());

        long unassigned = claimRepository.findByStatusOrderByCreatedAtAsc(ClaimStatus.SUBMITTED).size();
        redis.opsForValue().set(KEY_UNASSIGNED, String.valueOf(unassigned));
    }

    public void onClaimChanged(Claim previous, Claim current) {
        // Simple and consistent for MVP: rebuild from DB after each transition.
        rebuild();
    }

    public Map<String, Long> statusCounts() {
        return readHash(KEY_STATUS);
    }

    public Map<String, Long> marketCounts() {
        return readHash(KEY_MARKET);
    }

    public BigDecimal openReserve() {
        String value = redis.opsForValue().get(KEY_RESERVE);
        return value == null ? BigDecimal.ZERO : new BigDecimal(value);
    }

    public long unassignedCount() {
        String value = redis.opsForValue().get(KEY_UNASSIGNED);
        return value == null ? 0L : Long.parseLong(value);
    }

    private Map<String, Long> readHash(String key) {
        Map<Object, Object> raw = redis.opsForHash().entries(key);
        Map<String, Long> result = new HashMap<>();
        raw.forEach((k, v) -> result.put(String.valueOf(k), Long.parseLong(String.valueOf(v))));
        return result;
    }
}
