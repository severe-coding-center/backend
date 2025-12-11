package com.Guard.Back.Service;

import com.Guard.Back.Domain.*;
import com.Guard.Back.Dto.*;
import com.Guard.Back.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final ProtectedUserRepository protectedUserRepository;
    private final AlertLogRepository alertLogRepository;
    private final RelationshipRepository relationshipRepository;
    private final LocationLogRepository locationLogRepository;

    @Transactional(readOnly = true)
    public AdminDashboardDto getDashboardStats() {
        long totalProtected = protectedUserRepository.count();
        long totalGuardians = userRepository.count();
        long activeUsers = locationLogRepository.countActiveUsersSince(LocalDateTime.now().minusHours(24));

        ZonedDateTime startOfDay = LocalDate.now().atStartOfDay(ZoneId.of("Asia/Seoul"));
        long todaySos = alertLogRepository.countByEventTypeAndEventTimeAfter(EventType.SOS, startOfDay);
        List<SosLogDto> recentSos = getSosLogsInternal(5);

        return new AdminDashboardDto(totalProtected, totalGuardians, activeUsers, todaySos, recentSos);
    }

    @Transactional(readOnly = true)
    public List<GuardianListDto> getAllGuardians() {
        return userRepository.findAll().stream().map(user -> {
            int linkedCount = relationshipRepository.countByGuardian(user);
            return new GuardianListDto(
                    user.getId(), user.getProfileImage(), user.getNickname(),
                    user.getProviderId(), user.getProvider().toString(),
                    linkedCount, true
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SosLogDto> getAllSosLogs() {
        return getSosLogsInternal(100);
    }

    private List<SosLogDto> getSosLogsInternal(int limit) {
        return alertLogRepository.findByEventTypeOrderByEventTimeDesc(EventType.SOS).stream()
                .limit(limit)
                .map(log -> {
                    String guardianName = relationshipRepository.findFirstByProtectedUser(log.getProtectedUser())
                            .map(r -> r.getGuardian().getNickname()).orElse("연결 없음");
                    return new SosLogDto(
                            log.getId(), log.getProtectedUser().getDeviceId(),
                            log.getEventTime().toLocalDateTime(),
                            log.getLatitude() != null ? log.getLatitude() : 0.0,
                            log.getLongitude() != null ? log.getLongitude() : 0.0,
                            guardianName
                    );
                }).collect(Collectors.toList());
    }
}