package com.Guard.Back.Repository;

import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Domain.Relationship;
import com.Guard.Back.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

/*Relationship(관계) 엔티티에 대한 데이터 접근을 처리하는 Repository 인터페이스.*/
public interface RelationshipRepository extends JpaRepository<Relationship, Long> {

    /**
     * 특정 피보호자에게 연결된 보호자의 수를 계산합니다.
     * @param protectedUser 수를 계산할 피보호자 엔티티.
     * @return 연결된 보호자의 수 (long).
     */
    long countByProtectedUser(ProtectedUser protectedUser);

    /**
     * 특정 보호자와 연결된 모든 관계를 삭제합니다.
     * (회원 탈퇴 시 사용됩니다)
     * @param guardian 삭제할 관계의 기준이 되는 보호자 엔티티.
     */
    void deleteAllByGuardian(User guardian);

    /**
     * 특정 피보호자와 연결된 모든 관계를 삭제합니다.
     * (회원 탈퇴 시 사용됩니다)
     * @param protectedUser 삭제할 관계의 기준이 되는 피보호자 엔티티.
     */
    void deleteAllByProtectedUser(ProtectedUser protectedUser);

    /**
     * 특정 보호자와 피보호자 간의 관계가 이미 존재하는지 확인합니다.
     * @param guardian      확인할 보호자 엔티티.
     * @param protectedUser 확인할 피보호자 엔티티.
     * @return 관계가 존재하면 true, 아니면 false.
     */
    boolean existsByGuardianAndProtectedUser(User guardian, ProtectedUser protectedUser);

    /**
     * 특정 보호자(Guardian) 정보로 관계를 조회합니다.
     * @param guardian 관계를 조회할 보호자 엔티티.
     * @return 해당 보호자가 포함된 관계 정보를 담은 Optional 객체.
     */
    java.util.Optional<com.Guard.Back.Domain.Relationship> findByGuardian(User guardian);

}