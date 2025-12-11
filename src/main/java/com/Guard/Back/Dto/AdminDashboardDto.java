package com.Guard.Back.Dto;
import java.util.List;

public record AdminDashboardDto(
        long totalProtectedUsers,
        long totalGuardians,
        long activeUsers24h,
        long todaySosCount,
        List<SosLogDto> recentSosList
) {}