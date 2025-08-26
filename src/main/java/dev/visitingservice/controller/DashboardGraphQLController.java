package dev.visitingservice.controller;

import dev.visitingservice.dto.DashboardStatsDTO;
import dev.visitingservice.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class DashboardGraphQLController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardGraphQLController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @QueryMapping
    public DashboardStatsDTO tenantDashboardStats(@Argument String tenantId) {
        return dashboardService.getTenantStats(UUID.fromString(tenantId));
    }

    @QueryMapping
    public DashboardStatsDTO landlordDashboardStats(@Argument String landlordId) {
        return dashboardService.getLandlordStats(UUID.fromString(landlordId));
    }
}
