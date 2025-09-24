package com.Guard.Back.Repository;

import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Domain.Relationship;
import com.Guard.Back.Domain.User;
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

    /**
     * 특정 보호자와 연결된 모든 관계를 삭제합니다.
     * @param guardian 삭제할 보호자 객체
     */
    void deleteAllByGuardian(User guardian);

    /**
     * 💡특정 피보호자와 연결된 모든 관계를 삭제합니다.
     * @param protectedUser 삭제할 피보호자 객체
     */
    void deleteAllByProtectedUser(ProtectedUser protectedUser);

    /**
     * 💡 [추가] 특정 보호자와 피보호자 간의 관계가 이미 존재하는지 확인합니다.
     * @param guardian 확인할 보호자 객체
     * @param protectedUser 확인할 피보호자 객체
     * @return 관계가 존재하면 true, 아니면 false
     */
    boolean existsByGuardianAndProtectedUser(User guardian, ProtectedUser protectedUser);
}