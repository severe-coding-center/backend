package com.Guard.Back.Service;

import com.Guard.Back.Domain.AlertLog;
import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Dto.AlertLogDto;
import com.Guard.Back.Exception.CustomException;
import com.Guard.Back.Exception.ErrorCode;
import com.Guard.Back.Repository.AlertLogRepository;
import com.Guard.Back.Repository.ProtectedUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertService {
    private final AlertLogRepository alertLogRepository;
    private final ProtectedUserRepository protectedUserRepository;
    // GeofenceService와 동일하게 권한 검증 로직이 필요함

    @Transactional(readOnly = true)
    public List<AlertLogDto> getAlertLogs(Long protectedUserId) {
        // TODO: GeofenceService처럼 요청한 보호자가 권한이 있는지 확인하는 로직 추가 필요
        ProtectedUser pUser = protectedUserRepository.findById(protectedUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROTECTED_USER_NOT_FOUND));

        return alertLogRepository.findAllByProtectedUserOrderByEventTimeDesc(pUser)
                .stream()
                .map(log -> new AlertLogDto(log.getEventType(), log.getMessage(), log.getEventTime()))
                .collect(Collectors.toList());
    }
}