package com.Guard.Back.Repository;
import com.Guard.Back.Domain.Relationship;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RelationshipRepository extends JpaRepository<Relationship, Long> {}