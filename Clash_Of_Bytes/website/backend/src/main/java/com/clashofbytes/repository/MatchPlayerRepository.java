package com.clashofbytes.repository;

import com.clashofbytes.entity.MatchPlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchPlayerRepository extends JpaRepository<MatchPlayerEntity, Long> {
    List<MatchPlayerEntity> findByUserId(Long userId);

    @Query("SELECT mp.characterId, COUNT(mp) as cnt FROM MatchPlayerEntity mp WHERE mp.userId = :userId GROUP BY mp.characterId ORDER BY cnt DESC")
    List<Object[]> findFavoriteCharacterByUserId(@Param("userId") Long userId);
}
