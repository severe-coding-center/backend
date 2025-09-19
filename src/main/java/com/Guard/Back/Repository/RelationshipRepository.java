package com.Guard.Back.Repository;

import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Domain.Relationship;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 관계(Relationship) 엔티티에 대한 데이터 접근 계층.
 */
public interface RelationshipRepository extends JpaRepository<Relationship, Long> {

    /**
     * 특정 피보호자에게 연결된 보호자의 수를 계산합니다.
     * @param protectedUser 수를 계산할 피보호자 객체
     * @return 연결된 보호자의 수 (long)
     */
    long countByProtectedUser(ProtectedUser protectedUser);
}