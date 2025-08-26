package dev.visitingservice.service;

import dev.visitingservice.dto.DashboardStatsDTO;
import java.util.UUID;

public interface DashboardService {
    DashboardStatsDTO getTenantStats(UUID tenantId);
    DashboardStatsDTO getLandlordStats(UUID landlordId);
}
