package ru.shift.crm.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.shift.crm.dto.analytics.SellerBelowDto;
import ru.shift.crm.dto.analytics.TopSellerResponse;
import ru.shift.crm.service.AnalyticsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Validated
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/top-seller")
    public ResponseEntity<TopSellerResponse> getTopSeller(
            @RequestParam String period,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDateTime start;
        LocalDateTime end;
        switch (period.toLowerCase()) {
            case "day" -> {
                start = date.atStartOfDay();
                end = date.plusDays(1).atStartOfDay();
            }
            case "month" -> {
                start = date.withDayOfMonth(1).atStartOfDay();
                end = start.plusMonths(1);
            }
            case "quarter" -> {
                int month = ((date.getMonthValue() - 1) / 3) * 3 + 1;
                start = LocalDate.of(date.getYear(), month, 1).atStartOfDay();
                end = start.plusMonths(3);
            }
            case "year" -> {
                start = date.withDayOfYear(1).atStartOfDay();
                end = start.plusYears(1);
            }
            default -> throw new IllegalArgumentException("Период должен быть day, month, quarter или year");
        }
        return ResponseEntity.ok(analyticsService.getTopSellerByPeriod(start, end));
    }

    @GetMapping("/sellers-below")
    public ResponseEntity<List<SellerBelowDto>> getSellersBelow(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam @Positive BigDecimal threshold) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        return ResponseEntity.ok(analyticsService.getSellersBelowAmount(start, end, threshold));
    }
}