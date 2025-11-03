// TariffService.java
package com.example.tariffkey.service;

import com.example.tariffkey.model.Tariff;
import com.example.tariffkey.model.TariffRequest;
import com.example.tariffkey.model.TariffResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;


@Service
public class TariffService2 {

    private final JdbcTemplate jdbc;

    public TariffService2(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public TariffResponse calculate(TariffRequest req) {
        // --- 1) Parse and validate window ---
        Instant from = parseIso(req.getCalculationFrom());
        Instant to   = parseIso(req.getCalculationTo());
        if (from == null || to == null || !to.isAfter(from)) {
            throw new IllegalArgumentException("calculationFrom and calculationTo must be valid ISO-8601 and define a non-empty interval");
        }

        // --- 2) Base price & fixed fees (non-temporal for now) ---
        double basePrice = jdbc.queryForObject(
            "SELECT base_price FROM product WHERE code = ?",
            Double.class, req.getProduct()
        );

        double handlingFee   = req.isHandling()   ? getFee("handling")   : 0.0;
        double inspectionFee = req.isInspection() ? getFee("inspection") : 0.0;
        double processingFee = req.isProcessing() ? getFee("processing") : 0.0;
        double otherFees     = req.isOthers()     ? getFee("others")     : 0.0;
        double fixedFees     = handlingFee + inspectionFee + processingFee + otherFees;

        // --- 3) Pull all overlapping rows, then build effective segments with precedence ---
        List<RateRow> rows = fetchOverlappingRows(
            req.getProduct(), req.getFromCountry(), req.getToCountry(), from, to
        );
        List<Segment> segs = buildEffectiveSegments(rows, from, to);

        // If no tariff rows cover the period, result has zero tariff but still fees
        long totalSeconds = Duration.between(from, to).getSeconds();
        if (totalSeconds <= 0) {
            throw new IllegalArgumentException("Requested interval must be at least 1 second long");
        }

        // --- 4) Allocate quantity proportionally by time, compute amounts ---
        double totalItemPrice = 0.0;
        double totalTariff    = 0.0;
        List<TariffResponse.Segment> out = new ArrayList<>();

        for (Segment s : segs) {
            long segSec = Duration.between(s.from, s.to).getSeconds();
            if (segSec <= 0) continue;

            double portion = (double) segSec / (double) totalSeconds;
            double qtyPortion = req.getQuantity() * portion;

            double itemPrice = qtyPortion * basePrice;
            double tariffAmt = itemPrice * (s.ratePercent / 100.0);

            TariffResponse.Segment o = new TariffResponse.Segment();
            o.setFrom(OffsetDateTime.ofInstant(s.from, ZoneOffset.UTC).toString());
            o.setTo(OffsetDateTime.ofInstant(s.to, ZoneOffset.UTC).toString());
            o.setRatePercent(s.ratePercent);
            o.setQuantityPortion(qtyPortion);
            o.setItemPrice(itemPrice);
            o.setTariffAmount(tariffAmt);
            o.setLabel(s.label);
            o.setSource(s.source);
            out.add(o);

            totalItemPrice += itemPrice;
            totalTariff    += tariffAmt;
        }

        // Weighted-average rate (for summary)
        double weightedRate = (totalItemPrice == 0.0) ? 0.0 : (100.0 * totalTariff / totalItemPrice);

        // --- 5) Build response ---
        TariffResponse resp = new TariffResponse();
        resp.setItemPrice(totalItemPrice);
        resp.setTariffRate(weightedRate);
        resp.setTariffAmount(totalTariff);
        resp.setHandlingFee(handlingFee);
        resp.setInspectionFee(inspectionFee);
        resp.setProcessingFee(processingFee);
        resp.setOtherFees(otherFees);
        resp.setTotalPrice(totalItemPrice + totalTariff + fixedFees);
        resp.setSegments(out);
        return resp;
    }

    // ===== Helpers =====

    private Instant parseIso(String iso) {
        if (iso == null || iso.isBlank()) return null;
        // Accepts "YYYY-MM-DDTHH:mm:ss+08:00" etc.
        return OffsetDateTime.parse(iso).toInstant();
    }

    private double getFee(String fee) {
        return jdbc.queryForObject(
            "SELECT amount FROM fee_schedule WHERE fee = CAST(? AS fee_type)",
            Double.class, fee
        );
    }

    private static class RateRow {
        Instant from;
        Instant to;          // exclusive end (use a large instant if NULL)
        double ratePercent;
        boolean isOverride;
        String source;      
        String label;        
    }

    private static class Segment {
        Instant from;
        Instant to;
        double ratePercent;
        String label;
        String source;
    }
    private static Timestamp ts(Instant i) {
        return (i == null) ? null : Timestamp.from(i);
}

    /**
     * Fetch any route overrides and default rows that overlap [windowFrom, windowTo).
     * Assumes *_hist tables with (valid_from, valid_to), where valid_to NULL means open-ended.
     */
    private List<RateRow> fetchOverlappingRows(String product, String origin, String dest,
                                               Instant windowFrom, Instant windowTo) {
        List<RateRow> all = new ArrayList<>();

        // Route overrides (higher precedence)
        all.addAll(jdbc.query(
            "SELECT valid_from, valid_to, rate_percent " +
            "FROM route_tariff_override_hist " +
            "WHERE product_code = ? AND origin_country = ? AND dest_country = ? " +
            "  AND tstzrange(valid_from, valid_to, '[)') && tstzrange(?::timestamptz, ?::timestamptz, '[)') " +
            "ORDER BY valid_from",
            (rs, i) -> {
                RateRow r = new RateRow();
                Timestamp vf = rs.getTimestamp(1);
                Timestamp vt = rs.getTimestamp(2);
                r.from = vf.toInstant();
                r.to = (vt == null) ? Instant.ofEpochSecond(253402300799L) : vt.toInstant(); // 9999-12-31T23:59:59Z
                r.ratePercent = rs.getDouble(3);
                r.isOverride = true;
                r.source = "override";
                r.label = "Route override " + origin + "->" + dest + " (" + product + ")"; 
                return r;
            },
            product, origin, dest, ts(windowFrom), ts(windowTo)
        ));

        // Product defaults (fallback)
        all.addAll(jdbc.query(
            "SELECT valid_from, valid_to, rate_percent " +
            "FROM product_tariff_default_hist " +
            "WHERE product_code = ? " +
            "  AND tstzrange(valid_from, valid_to, '[)') && tstzrange(?::timestamptz, ?::timestamptz, '[)') " +
            "ORDER BY valid_from",
            (rs, i) -> {
                RateRow r = new RateRow();
                Timestamp vf = rs.getTimestamp(1);
                Timestamp vt = rs.getTimestamp(2);
                r.from = vf.toInstant();
                r.to = (vt == null) ? Instant.ofEpochSecond(253402300799L) : vt.toInstant();
                r.ratePercent = rs.getDouble(3);
                r.isOverride = false;
                r.source = "default";
                r.label = "Default rate (" + product + ")";
                return r;
            },
            product, ts(windowFrom), ts(windowTo)
        ));

        return all;
    }

    /**
     * Build a non-overlapping effective timeline over [windowFrom, windowTo),
     * where for each slice:
     *   - If any override fully covers the slice, use the (most recent start) override
     *   - Else if a default fully covers the slice, use that default
     *   - Else (no coverage), skip the slice (no tariff in force)
     */
    private List<Segment> buildEffectiveSegments(List<RateRow> rows, Instant windowFrom, Instant windowTo) {
        // 1) Collect all cut points: window bounds + row bounds clipped to window
        TreeSet<Instant> cuts = new TreeSet<>();
        cuts.add(windowFrom);
        cuts.add(windowTo);
        for (RateRow r : rows) {
            Instant a = r.from.isBefore(windowFrom) ? windowFrom : r.from;
            Instant b = r.to.isAfter(windowTo) ? windowTo : r.to;
            if (a.isBefore(b)) {
                cuts.add(a);
                cuts.add(b);
            }
        }

        // 2) For each consecutive [a,b), decide the chosen rate
        List<Instant> pts = new ArrayList<>(cuts);
        List<Segment> segs = new ArrayList<>();
        for (int i = 0; i + 1 < pts.size(); i++) {
            Instant a = pts.get(i);
            Instant b = pts.get(i + 1);

            RateRow chosen = chooseCovering(rows, a, b, true);   // try override first
            if (chosen == null) {
                chosen = chooseCovering(rows, a, b, false);      // then default
            }
            if (chosen == null) {
                // No tariff covering this sub-interval — skip
                continue;
            }

            Segment s = new Segment();
            s.from = a;
            s.to = b;
            s.ratePercent = chosen.ratePercent;
            s.label = chosen.label;
            s.source = chosen.source;
            segs.add(s);
        }
        return segs;
    }

    /**
     * Pick a row that fully covers [a,b) with required precedence (override or default).
     * If multiple match, prefer the one with the latest 'from' (closest to 'a').
     */
    private RateRow chooseCovering(List<RateRow> rows, Instant a, Instant b, boolean wantOverride) {
        RateRow best = null;
        for (RateRow r : rows) {
            if (r.isOverride != wantOverride) continue;
            if (!r.from.isAfter(a) && !r.to.isBefore(b)) {
                // r.from <= a && r.to >= b
                if (best == null || r.from.isAfter(best.from)) {
                    best = r;
                }
            }
        }
        return best;
    }

    // ====== In-memory tariff management (for Admin page) ======
    private final List<Tariff> tariffs = new ArrayList<>();
    private int nextId = 1;

    public List<Tariff> getAllTariffs() {
        return tariffs;
    }

    public Tariff addTariff(Tariff tariff) {
        tariff.setId(nextId++);
        tariffs.add(tariff);
        return tariff;
    }

    public void deleteTariff(int id) {
        tariffs.removeIf(t -> t.getId() == id);
    }
}
