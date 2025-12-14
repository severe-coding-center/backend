package com.Guard.Back.Repository;

import com.Guard.Back.Domain.LocationLog;
import com.Guard.Back.Domain.ProtectedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

import java.util.Optional;

/**
 * LocationLog 엔티티에 대한 데이터 접근을 처리하는 Repository 인터페이스.
 * Spring Data JPA에 의해 자동으로 구현
 */
public interface LocationLogRepository extends JpaRepository<LocationLog, Long> {

    /**
     * 특정 피보호자의 가장 최신 위치 기록 1개를 ID 내림차순으로 조회
     * @param protectedUser 위치 기록을 조회할 피보호자 엔티티.
     * @return 가장 최신 위치 기록을 담은 Optional 객체. 기록이 없으면 Optional.empty()를 반환
     */
    Optional<LocationLog> findTopByProtectedUserOrderByIdDesc(ProtectedUser protectedUser);

    @Query("SELECT COUNT(DISTINCT l.protectedUser) FROM LocationLog l WHERE l.recordedAt >= :time")
    long countActiveUsersSince(@Param("time") LocalDateTime time);
}