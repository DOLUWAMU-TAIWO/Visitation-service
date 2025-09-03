package dev.visitingservice.controller;

import dev.visitingservice.dto.ApiResponse;
import dev.visitingservice.dto.PriceQuoteRequestDTO;
import dev.visitingservice.dto.PriceQuoteResponseDTO;
import dev.visitingservice.service.PricingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pricing")

public class PricingController {

    @Autowired
    private PricingService pricingService;

    @PostMapping("/quote")
    public ResponseEntity<ApiResponse<PriceQuoteResponseDTO>> generatePriceQuote(
            @Valid @RequestBody PriceQuoteRequestDTO request) {

        PriceQuoteResponseDTO quote = pricingService.generatePriceQuote(request);

        return ResponseEntity.ok(
            new ApiResponse<>(true, "Price quote generated successfully", quote)
        );
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
            new ApiResponse<>(true, "Pricing service is healthy", "OK")
        );
    }
}
