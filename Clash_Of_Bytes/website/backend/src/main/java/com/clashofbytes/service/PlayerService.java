package com.clashofbytes.service;

import com.clashofbytes.dto.PlayerStatsResponse;
import com.clashofbytes.entity.CharacterEntity;
import com.clashofbytes.entity.MatchPlayerEntity;
import com.clashofbytes.entity.User;
import com.clashofbytes.repository.CharacterRepository;
import com.clashofbytes.repository.MatchPlayerRepository;
import com.clashofbytes.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerService {

    private final UserRepository userRepository;
    private final MatchPlayerRepository matchPlayerRepository;
    private final CharacterRepository characterRepository;

    @Autowired
    public PlayerService(UserRepository userRepository,
                         MatchPlayerRepository matchPlayerRepository,
                         CharacterRepository characterRepository) {
        this.userRepository = userRepository;
        this.matchPlayerRepository = matchPlayerRepository;
        this.characterRepository = characterRepository;
    }

    public PlayerStatsResponse getPlayerStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        List<MatchPlayerEntity> history = matchPlayerRepository.findByUserId(userId);

        int totalMatches = history.size();
        int wins = 0;
        int losses = 0;
        int draws = 0;
        int totalScore = 0;

        for (MatchPlayerEntity mp : history) {
            totalScore += mp.getFinalScore();
            if ("WIN".equalsIgnoreCase(mp.getResult())) wins++;
            else if ("LOSS".equalsIgnoreCase(mp.getResult())) losses++;
            else draws++;
        }

        double winRate = (totalMatches > 0) ? Math.round(((double) wins / totalMatches) * 10000.0) / 100.0 : 0.0;

        String favChar = "None";
        List<Object[]> favList = matchPlayerRepository.findFavoriteCharacterByUserId(userId);
        if (!favList.isEmpty()) {
            Long favCharId = (Long) favList.get(0)[0];
            favChar = characterRepository.findById(favCharId)
                    .map(CharacterEntity::getCharacterName)
                    .orElse("Character #" + favCharId);
        }

        PlayerStatsResponse res = new PlayerStatsResponse();
        res.setUserId(user.getUserId());
        res.setUsername(user.getUsername());
        res.setTotalMatches(totalMatches);
        res.setWins(wins);
        res.setLosses(losses);
        res.setDraws(draws);
        res.setWinRate(winRate);
        res.setTotalScore(totalScore);
        res.setFavoriteCharacter(favChar);

        return res;
    }
}
