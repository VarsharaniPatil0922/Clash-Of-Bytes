package com.clashofbytes.controller;

import com.clashofbytes.dto.PlayerStatsResponse;
import com.clashofbytes.entity.MatchEntity;
import com.clashofbytes.repository.MatchRepository;
import com.clashofbytes.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@CrossOrigin(origins = "*")
public class PlayerController {

    private final PlayerService playerService;
    private final MatchRepository matchRepository;

    @Autowired
    public PlayerController(PlayerService playerService, MatchRepository matchRepository) {
        this.playerService = playerService;
        this.matchRepository = matchRepository;
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<PlayerStatsResponse> getPlayerStats(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getPlayerStats(id));
    }

    @GetMapping("/{id}/matches")
    public ResponseEntity<List<MatchEntity>> getPlayerMatches(@PathVariable Long id) {
        return ResponseEntity.ok(matchRepository.findMatchesByUserId(id));
    }
}
