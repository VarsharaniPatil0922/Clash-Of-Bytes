package com.clashofbytes.service;

import com.clashofbytes.dto.MatchCreateRequest;
import com.clashofbytes.entity.MatchEntity;
import com.clashofbytes.entity.MatchPlayerEntity;
import com.clashofbytes.entity.MatchRoundEntity;
import com.clashofbytes.repository.MatchPlayerRepository;
import com.clashofbytes.repository.MatchRepository;
import com.clashofbytes.repository.MatchRoundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchPlayerRepository matchPlayerRepository;
    private final MatchRoundRepository matchRoundRepository;

    @Autowired
    public MatchService(MatchRepository matchRepository,
                        MatchPlayerRepository matchPlayerRepository,
                        MatchRoundRepository matchRoundRepository) {
        this.matchRepository = matchRepository;
        this.matchPlayerRepository = matchPlayerRepository;
        this.matchRoundRepository = matchRoundRepository;
    }

    @Transactional
    public MatchEntity recordMatch(MatchCreateRequest req) {
        if (req.getPlayer1UserId().equals(req.getPlayer2UserId())) {
            throw new IllegalArgumentException("Player 1 and Player 2 cannot be the same user account.");
        }

        MatchEntity match = new MatchEntity();
        match.setMatchDate(LocalDateTime.now());
        match.setPlayer1UserId(req.getPlayer1UserId());
        match.setPlayer2UserId(req.getPlayer2UserId());
        match.setTotalRounds(req.getTotalRounds() != null ? req.getTotalRounds() : 3);
        match.setPlayer1FinalScore(req.getPlayer1FinalScore() != null ? req.getPlayer1FinalScore() : 0);
        match.setPlayer2FinalScore(req.getPlayer2FinalScore() != null ? req.getPlayer2FinalScore() : 0);
        match.setMatchStatus("COMPLETED");

        // Compute or assign winner
        if (req.getWinnerUserId() != null) {
            match.setWinnerUserId(req.getWinnerUserId());
        } else if (match.getPlayer1FinalScore() > match.getPlayer2FinalScore()) {
            match.setWinnerUserId(req.getPlayer1UserId());
        } else if (match.getPlayer2FinalScore() > match.getPlayer1FinalScore()) {
            match.setWinnerUserId(req.getPlayer2UserId());
        } else {
            match.setWinnerUserId(null); // Draw
        }

        MatchEntity savedMatch = matchRepository.save(match);

        // Player 1 Entry
        MatchPlayerEntity mp1 = new MatchPlayerEntity();
        mp1.setMatch(savedMatch);
        mp1.setUserId(req.getPlayer1UserId());
        mp1.setCharacterId(req.getPlayer1CharacterId() != null ? req.getPlayer1CharacterId() : 4L);
        mp1.setFinalScore(savedMatch.getPlayer1FinalScore());
        mp1.setResult(savedMatch.getWinnerUserId() == null ? "DRAW" :
                (savedMatch.getWinnerUserId().equals(req.getPlayer1UserId()) ? "WIN" : "LOSS"));
        matchPlayerRepository.save(mp1);

        // Player 2 Entry
        MatchPlayerEntity mp2 = new MatchPlayerEntity();
        mp2.setMatch(savedMatch);
        mp2.setUserId(req.getPlayer2UserId());
        mp2.setCharacterId(req.getPlayer2CharacterId() != null ? req.getPlayer2CharacterId() : 3L);
        mp2.setFinalScore(savedMatch.getPlayer2FinalScore());
        mp2.setResult(savedMatch.getWinnerUserId() == null ? "DRAW" :
                (savedMatch.getWinnerUserId().equals(req.getPlayer2UserId()) ? "WIN" : "LOSS"));
        matchPlayerRepository.save(mp2);

        // Save Individual Rounds
        if (req.getRounds() != null && !req.getRounds().isEmpty()) {
            for (MatchCreateRequest.RoundTelemetry r : req.getRounds()) {
                MatchRoundEntity round = new MatchRoundEntity();
                round.setMatch(savedMatch);
                round.setRoundNumber(r.getRoundNumber());
                round.setPlayer1Score(r.getP1Score());
                round.setPlayer2Score(r.getP2Score());
                if (r.getWinnerPlayerIndex() != null) {
                    round.setWinnerUserId(r.getWinnerPlayerIndex() == 1 ? req.getPlayer1UserId() : req.getPlayer2UserId());
                }
                round.setRoundStatus("COMPLETED");
                matchRoundRepository.save(round);
            }
        }

        return savedMatch;
    }

    public List<MatchEntity> getAllMatches() {
        return matchRepository.findAllByOrderByMatchDateDesc();
    }

    public Optional<MatchEntity> getMatchById(Long id) {
        return matchRepository.findById(id);
    }
}
