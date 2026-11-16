package com.clashofbytes.service;

import com.clashofbytes.dto.LeaderboardEntry;
import com.clashofbytes.dto.PlayerStatsResponse;
import com.clashofbytes.entity.User;
import com.clashofbytes.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class LeaderboardService {

    private final UserRepository userRepository;
    private final PlayerService playerService;

    @Autowired
    public LeaderboardService(UserRepository userRepository, PlayerService playerService) {
        this.userRepository = userRepository;
        this.playerService = playerService;
    }

    public List<LeaderboardEntry> getLeaderboard() {
        List<User> users = userRepository.findAll();
        List<LeaderboardEntry> entries = new ArrayList<>();

        for (User u : users) {
            PlayerStatsResponse stats = playerService.getPlayerStats(u.getUserId());
            entries.add(new LeaderboardEntry(
                    0,
                    u.getUserId(),
                    u.getUsername(),
                    stats.getTotalMatches(),
                    stats.getWins(),
                    stats.getLosses(),
                    stats.getWinRate(),
                    stats.getTotalScore(),
                    stats.getFavoriteCharacter()
            ));
        }

        // Sort primarily by wins DESC, secondarily by totalScore DESC
        entries.sort(Comparator.comparingInt(LeaderboardEntry::getWins)
                .thenComparingInt(LeaderboardEntry::getTotalScore)
                .reversed());

        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRank(i + 1);
        }

        return entries;
    }
}
