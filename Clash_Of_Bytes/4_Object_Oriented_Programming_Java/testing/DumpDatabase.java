package testing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Utility to dump actual SQL query outputs across all 5 database tables.
 */
public class DumpDatabase {
    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:clash_of_bytes.db");
        Statement s = conn.createStatement();

        System.out.println("=== USERS TABLE ===");
        ResultSet rs = s.executeQuery("SELECT user_id, username, email, created_at FROM users;");
        while (rs.next()) {
            System.out.printf("User #%d: %-20s | %-28s | Created: %s%n",
                    rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4));
        }

        System.out.println("\n=== CHARACTERS TABLE (6 Fighters) ===");
        rs = s.executeQuery("SELECT character_id, character_name, archetype, base_hp, base_attack, speed FROM characters;");
        while (rs.next()) {
            System.out.printf("Char #%d: %-18s | %-16s | HP: %5.1f | ATK: %4.1f | SPD: %3.1f%n",
                    rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getDouble(5), rs.getDouble(6));
        }

        System.out.println("\n=== MATCHES TABLE ===");
        rs = s.executeQuery("SELECT match_id, match_date, player1_user_id, player2_user_id, winner_user_id, total_rounds, match_status, player1_final_score, player2_final_score FROM matches;");
        while (rs.next()) {
            System.out.printf("Match #%d [%s]: P1(User %d) Score: %d vs P2(User %d) Score: %d | Winner: User %s | Rounds: %d [%s]%n",
                    rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getInt(8),
                    rs.getInt(4), rs.getInt(9), rs.getString(5), rs.getInt(6), rs.getString(7));
        }

        System.out.println("\n=== MATCH_PLAYERS TABLE (2 Records Per Match) ===");
        rs = s.executeQuery("SELECT match_player_id, match_id, user_id, character_id, final_score, result FROM match_players;");
        while (rs.next()) {
            System.out.printf("MP #%d | Match #%d | User: %d | Char: %d | Score: %3d | Result: %s%n",
                    rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getString(6));
        }

        System.out.println("\n=== MATCH_ROUNDS TABLE ===");
        rs = s.executeQuery("SELECT round_id, match_id, round_number, winner_user_id, player1_score, player2_score, round_status FROM match_rounds;");
        while (rs.next()) {
            System.out.printf("Round #%d (Match %d, R%d) -> Winner: User %s | P1: %d pts vs P2: %d pts [%s]%n",
                    rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getString(4), rs.getInt(5), rs.getInt(6), rs.getString(7));
        }
    }
}
