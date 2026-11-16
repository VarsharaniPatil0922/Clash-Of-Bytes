package com.clashofbytes.repository;

import com.clashofbytes.entity.MatchRoundEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRoundRepository extends JpaRepository<MatchRoundEntity, Long> {
    List<MatchRoundEntity> findByMatch_MatchIdOrderByRoundNumberAsc(Long matchId);
}
