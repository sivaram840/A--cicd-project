package com.example.expensesplitter.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class SplitUtil {

    // Convert BigDecimal amount (scale up to 2 decimals) to total cents (long)
    public static long toCents(BigDecimal amount) {
        if (amount == null) throw new IllegalArgumentException("amount null");
        if (amount.scale() > 2) {
            throw new IllegalArgumentException("Amount has more than 2 decimal places");
        }
        return amount.movePointRight(2).longValueExact();
    }

    public static BigDecimal centsToBigDecimal(long cents) {
        return new BigDecimal(cents).movePointLeft(2).setScale(2, RoundingMode.HALF_EVEN);
    }

    // EQUAL split: deterministic distribution of leftover cents to first N userIds (sorted ascending)
    public static Map<Long, Long> allocateEqual(long totalCents, List<Long> userIds) {
        Map<Long, Long> res = new LinkedHashMap<>();
        if (userIds == null || userIds.isEmpty()) return res;
        List<Long> sorted = new ArrayList<>(userIds);
        Collections.sort(sorted);
        int n = sorted.size();
        long base = totalCents / n;
        long remainder = totalCents % n;
        for (int i = 0; i < n; i++) {
            long add = (i < remainder) ? 1L : 0L;
            res.put(sorted.get(i), base + add);
        }
        return res;
    }

    // PERCENT split: percentMap (userId -> percent as BigDecimal). Percent values sum to ~100.
    // We compute exact cents per user, floor them, then distribute leftover cents by largest fractional parts.
    public static Map<Long, Long> allocatePercent(long totalCents, Map<Long, BigDecimal> percentMap) {
        Map<Long, Long> floors = new LinkedHashMap<>();
        Map<Long, BigDecimal> fractions = new HashMap<>();
        BigDecimal totalCentsBD = new BigDecimal(totalCents);

        long sumFloors = 0L;
        for (Map.Entry<Long, BigDecimal> e : percentMap.entrySet()) {
            Long uid = e.getKey();
            BigDecimal percent = e.getValue();
            if (percent == null) percent = BigDecimal.ZERO;
            BigDecimal exact = totalCentsBD.multiply(percent).divide(new BigDecimal(100), 10, RoundingMode.HALF_EVEN);
            long floor = exact.setScale(0, RoundingMode.DOWN).longValue();
            BigDecimal fraction = exact.subtract(new BigDecimal(floor));
            floors.put(uid, floor);
            fractions.put(uid, fraction);
            sumFloors += floor;
        }
        long remainder = totalCents - sumFloors;
        // sort by fractional desc, tie-break by userId asc
        List<Long> users = new ArrayList<>(fractions.keySet());
        users.sort((a, b) -> {
            int cmp = fractions.get(b).compareTo(fractions.get(a));
            if (cmp != 0) return cmp;
            return a.compareTo(b);
        });
        Map<Long, Long> result = new LinkedHashMap<>();
        for (Long uid : users) result.put(uid, floors.get(uid));
        for (int i = 0; i < remainder; i++) {
            Long uid = users.get(i % users.size());
            result.put(uid, result.get(uid) + 1L);
        }
        return result;
    }

    // CUSTOM split: amountMap (userId -> BigDecimal amount). Validate sum equals totalCents.
    public static Map<Long, Long> allocateCustom(long totalCents, Map<Long, BigDecimal> amountMap) {
        Map<Long, Long> res = new LinkedHashMap<>();
        long sum = 0L;
        for (Map.Entry<Long, BigDecimal> e : amountMap.entrySet()) {
            long cents = toCents(e.getValue());
            res.put(e.getKey(), cents);
            sum += cents;
        }
        if (sum != totalCents) {
            throw new IllegalArgumentException("Custom shares do not sum to total amount (in cents). expected=" + totalCents + " actual=" + sum);
        }
        return res;
    }
}

