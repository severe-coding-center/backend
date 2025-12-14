package com.Guard.Back.Repository;

import com.Guard.Back.Domain.AlertLog;
import com.Guard.Back.Domain.EventType;
import com.Guard.Back.Domain.ProtectedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface AlertLogRepository extends JpaRepository<AlertLog, Long> {
    // 특정 피보호자의 모든 기록을 최신순으로 조회
    List<AlertLog> findAllByProtectedUserOrderByEventTimeDesc(ProtectedUser protectedUser);

    // 오늘 SOS 카운트
    long countByEventTypeAndEventTimeAfter(EventType eventType, ZonedDateTime time);

    // SOS 전체 이력 조회
    List<AlertLog> findByEventType(EventType eventType, Pageable pageable);
}