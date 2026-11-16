package com.clashofbytes.repository;

import com.clashofbytes.entity.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<MatchEntity, Long> {
    List<MatchEntity> findAllByOrderByMatchDateDesc();

    @Query("SELECT m FROM MatchEntity m WHERE m.player1UserId = :userId OR m.player2UserId = :userId ORDER BY m.matchDate DESC")
    List<MatchEntity> findMatchesByUserId(@Param("userId") Long userId);
}
