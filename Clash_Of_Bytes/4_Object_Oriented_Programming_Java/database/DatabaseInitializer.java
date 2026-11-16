package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes and verifies the relational schema for Clash of Bytes.
 * Auto-creates tables, indexes, and pre-populates the fighter characters table.
 */
public class DatabaseInitializer {

    public static void initializeDatabase() throws SQLException {
        Connection conn = DBConnection.getConnection();
        boolean isPostgres = conn.getMetaData().getDatabaseProductName().toLowerCase().contains("postgres");

        String autoInc = isPostgres ? "SERIAL PRIMARY KEY" : "INTEGER PRIMARY KEY AUTOINCREMENT";

        try (Statement stmt = conn.createStatement()) {
            // 1. Users Table
            stmt.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "user_id " + autoInc + ", "
                    + "username VARCHAR(50) NOT NULL UNIQUE, "
                    + "email VARCHAR(100) NOT NULL UNIQUE, "
                    + "password_hash VARCHAR(128) NOT NULL, "
                    + "salt VARCHAR(64) NOT NULL, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ");");

            // 2. Characters Table
            stmt.execute("CREATE TABLE IF NOT EXISTS characters ("
                    + "character_id " + autoInc + ", "
                    + "character_name VARCHAR(50) NOT NULL UNIQUE, "
                    + "character_description VARCHAR(255) NOT NULL, "
                    + "archetype VARCHAR(50) NOT NULL, "
                    + "base_hp REAL NOT NULL, "
                    + "base_attack REAL NOT NULL, "
                    + "base_defense REAL NOT NULL, "
                    + "speed REAL NOT NULL"
                    + ");");

            // 3. Matches Table
            stmt.execute("CREATE TABLE IF NOT EXISTS matches ("
                    + "match_id " + autoInc + ", "
                    + "match_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "player1_user_id INTEGER NOT NULL, "
                    + "player2_user_id INTEGER NOT NULL, "
                    + "winner_user_id INTEGER, "
                    + "total_rounds INTEGER NOT NULL, "
                    + "match_status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED', "
                    + "player1_final_score INTEGER NOT NULL, "
                    + "player2_final_score INTEGER NOT NULL, "
                    + "FOREIGN KEY (player1_user_id) REFERENCES users(user_id) ON DELETE CASCADE, "
                    + "FOREIGN KEY (player2_user_id) REFERENCES users(user_id) ON DELETE CASCADE, "
                    + "FOREIGN KEY (winner_user_id) REFERENCES users(user_id) ON DELETE SET NULL"
                    + ");");

            // 4. Match Players Table
            stmt.execute("CREATE TABLE IF NOT EXISTS match_players ("
                    + "match_player_id " + autoInc + ", "
                    + "match_id INTEGER NOT NULL, "
                    + "user_id INTEGER NOT NULL, "
                    + "character_id INTEGER NOT NULL, "
                    + "final_score INTEGER NOT NULL, "
                    + "result VARCHAR(10) NOT NULL, "
                    + "FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE, "
                    + "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE, "
                    + "FOREIGN KEY (character_id) REFERENCES characters(character_id) ON DELETE CASCADE"
                    + ");");

            // 5. Match Rounds Table
            stmt.execute("CREATE TABLE IF NOT EXISTS match_rounds ("
                    + "round_id " + autoInc + ", "
                    + "match_id INTEGER NOT NULL, "
                    + "round_number INTEGER NOT NULL, "
                    + "winner_user_id INTEGER, "
                    + "player1_score INTEGER NOT NULL, "
                    + "player2_score INTEGER NOT NULL, "
                    + "round_status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED', "
                    + "FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE, "
                    + "FOREIGN KEY (winner_user_id) REFERENCES users(user_id) ON DELETE SET NULL"
                    + ");");

            // Indexes for Fast Lookups
            try {
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_matches_p1 ON matches(player1_user_id);");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_matches_p2 ON matches(player2_user_id);");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_match_players_match ON match_players(match_id);");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_match_players_user ON match_players(user_id);");
            } catch (SQLException ignored) {}
        }

        // Seed Characters Roster if empty
        seedCharacters(conn);
    }

    private static void seedCharacters(Connection conn) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM characters;";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = "INSERT INTO characters (character_name, character_description, archetype, base_hp, base_attack, base_defense, speed) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?);";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    Object[][] defaultFighters = {
                        {"Ironclad Warrior", "Armored heavy vanguard equipped with crushing greatsword slashes and Titan Guard.", "WarriorFighter", 115.0, 18.0, 6.0, 5.0},
                        {"Blaze Berserker", "Reckless brawler trading armor defense for devastating fury damage multipliers.", "BerserkerWarrior", 130.0, 22.0, 4.0, 6.5},
                        {"Shadow Ninja", "Stealth assassin utilizing blinding agility, evasion frames, and shuriken strikes.", "NinjaFighter", 95.0, 16.0, 4.0, 9.0},
                        {"Cyber Brawler", "High-tech martial artist featuring rapid nanotech combo rushes and cyber barriers.", "CyberBrawler", 110.0, 19.0, 3.0, 7.0},
                        {"Aura Paladin", "Radiant holy champion channeling blessed aegis barriers and holy radiance strikes.", "PaladinFighter", 120.0, 17.0, 7.5, 4.5},
                        {"Titan Gladiator", "Colosseum champion master combining raw berserker fury with elemental magma strikes.", "HybridGladiator", 140.0, 24.0, 8.0, 4.0}
                    };

                    for (Object[] row : defaultFighters) {
                        ps.setString(1, (String) row[0]);
                        ps.setString(2, (String) row[1]);
                        ps.setString(3, (String) row[2]);
                        ps.setDouble(4, (Double) row[3]);
                        ps.setDouble(5, (Double) row[4]);
                        ps.setDouble(6, (Double) row[5]);
                        ps.setDouble(7, (Double) row[6]);
                        ps.executeUpdate();
                    }
                }
            }
        }
    }
}
