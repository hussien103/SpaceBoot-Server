package org.example.repository;

import org.example.entity.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {
    Optional<PlayerEntity> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    @Query("SELECT p FROM PlayerEntity p ORDER BY p.totalScore DESC")
    List<PlayerEntity> findAllOrderByTotalScoreDesc();
}

