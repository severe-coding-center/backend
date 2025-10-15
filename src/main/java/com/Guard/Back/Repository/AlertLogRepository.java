package com.Guard.Back.Repository;

import com.Guard.Back.Domain.AlertLog;
import com.Guard.Back.Domain.ProtectedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertLogRepository extends JpaRepository<AlertLog, Long> {
    // 특정 피보호자의 모든 기록을 최신순으로 조회
    List<AlertLog> findAllByProtectedUserOrderByEventTimeDesc(ProtectedUser protectedUser);
}