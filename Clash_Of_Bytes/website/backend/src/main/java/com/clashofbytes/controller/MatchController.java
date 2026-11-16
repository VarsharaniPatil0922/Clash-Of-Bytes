package com.clashofbytes.controller;

import com.clashofbytes.dto.MatchCreateRequest;
import com.clashofbytes.entity.MatchEntity;
import com.clashofbytes.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "*")
public class MatchController {

    private final MatchService matchService;

    @Autowired
    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping
    public ResponseEntity<MatchEntity> recordMatch(@RequestBody MatchCreateRequest req) {
        MatchEntity match = matchService.recordMatch(req);
        return ResponseEntity.ok(match);
    }

    @GetMapping
    public ResponseEntity<List<MatchEntity>> getAllMatches() {
        return ResponseEntity.ok(matchService.getAllMatches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchEntity> getMatchById(@PathVariable Long id) {
        return matchService.getMatchById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
