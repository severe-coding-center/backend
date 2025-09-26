package com.Guard.Back.Repository;

import com.Guard.Back.Domain.LocationLog;
import com.Guard.Back.Domain.ProtectedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LocationLogRepository extends JpaRepository<LocationLog, Long> {
    // 💡 [변경] 특정 피보호자의 가장 최신 위치 기록 1개를 조회합니다.
    Optional<LocationLog> findTopByProtectedUserOrderByIdDesc(ProtectedUser protectedUser);
}