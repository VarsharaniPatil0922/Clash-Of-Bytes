#!/usr/bin/env python3
"""
=======================================================================
 CLASH OF BYTES: SQLITE TO SUPABASE POSTGRESQL MIGRATION UTILITY
 Exports all existing users, matches, players, and round telemetry
 from local SQLite database (clash_of_bytes.db) to Supabase PostgreSQL.
=======================================================================
"""

import os
import sqlite3
import sys

SQLITE_PATH = os.path.join(os.path.dirname(__file__), "..", "..", "clash_of_bytes.db")
SQLITE_PATH = os.path.abspath(SQLITE_PATH)
OUTPUT_SQL_PATH = os.path.join(os.path.dirname(__file__), "supabase_data_dump.sql")

def escape_str(val):
    if val is None:
        return "NULL"
    s = str(val).replace("'", "''")
    return f"'{s}'"

def num_or_null(val):
    if val is None:
        return "NULL"
    return str(val)

def export_to_supabase_sql():
    if not os.path.exists(SQLITE_PATH):
        print(f"[ERROR] Source SQLite database not found at: {SQLITE_PATH}")
        sys.exit(1)

    print(f"[*] Opening SQLite database: {SQLITE_PATH}")
    conn = sqlite3.connect(SQLITE_PATH)
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()

    lines = []
    lines.append("-- =======================================================================")
    lines.append("-- CLASH OF BYTES: SUPABASE DATA DUMP FROM SQLITE DATABASE")
    lines.append("-- Auto-generated migration script for Supabase PostgreSQL")
    lines.append("-- =======================================================================")
    lines.append("BEGIN TRANSACTION;")
    lines.append("")

    # 1. Users
    cursor.execute("SELECT user_id, username, email, password_hash, salt, created_at FROM users ORDER BY user_id")
    users = cursor.fetchall()
    print(f"[*] Exporting {len(users)} users...")
    lines.append(f"-- 1. USERS ({len(users)} records)")
    for u in users:
        created = escape_str(u["created_at"]) if u["created_at"] else "CURRENT_TIMESTAMP"
        lines.append(
            f"INSERT INTO users (user_id, username, email, password_hash, salt, created_at) "
            f"VALUES ({u['user_id']}, {escape_str(u['username'])}, {escape_str(u['email'])}, "
            f"{escape_str(u['password_hash'])}, {escape_str(u['salt'])}, {created}) "
            f"ON CONFLICT (user_id) DO NOTHING;"
        )
    lines.append("")

    # 2. Characters
    cursor.execute("SELECT character_id, character_name, character_description, archetype, base_hp, base_attack, base_defense, speed FROM characters ORDER BY character_id")
    chars = cursor.fetchall()
    print(f"[*] Exporting {len(chars)} characters...")
    lines.append(f"-- 2. CHARACTERS ({len(chars)} records)")
    for c in chars:
        lines.append(
            f"INSERT INTO characters (character_id, character_name, character_description, archetype, base_hp, base_attack, base_defense, speed) "
            f"VALUES ({c['character_id']}, {escape_str(c['character_name'])}, {escape_str(c['character_description'])}, "
            f"{escape_str(c['archetype'])}, {c['base_hp']}, {c['base_attack']}, {c['base_defense']}, {c['speed']}) "
            f"ON CONFLICT (character_id) DO UPDATE SET "
            f"character_name = EXCLUDED.character_name, character_description = EXCLUDED.character_description, "
            f"archetype = EXCLUDED.archetype, base_hp = EXCLUDED.base_hp, base_attack = EXCLUDED.base_attack, "
            f"base_defense = EXCLUDED.base_defense, speed = EXCLUDED.speed;"
        )
    lines.append("")

    # 3. Matches
    cols = [r[1] for r in cursor.execute("PRAGMA table_info(matches)")]
    cursor.execute("SELECT * FROM matches ORDER BY match_id")
    matches = cursor.fetchall()
    print(f"[*] Exporting {len(matches)} matches...")
    lines.append(f"-- 3. MATCHES ({len(matches)} records)")
    for m in matches:
        m_date = escape_str(m["match_date"]) if m["match_date"] else "CURRENT_TIMESTAMP"
        loser_id = num_or_null(m["loser_user_id"] if "loser_user_id" in cols else None)
        dur = num_or_null(m["match_duration_seconds"] if "match_duration_seconds" in cols else 0)
        p1_won = num_or_null(m["player1_rounds_won"] if "player1_rounds_won" in cols else 0)
        p2_won = num_or_null(m["player2_rounds_won"] if "player2_rounds_won" in cols else 0)
        lines.append(
            f"INSERT INTO matches (match_id, match_date, player1_user_id, player2_user_id, winner_user_id, loser_user_id, "
            f"total_rounds, player1_rounds_won, player2_rounds_won, match_status, player1_final_score, player2_final_score, match_duration_seconds) "
            f"VALUES ({m['match_id']}, {m_date}, {m['player1_user_id']}, {m['player2_user_id']}, "
            f"{num_or_null(m['winner_user_id'])}, {loser_id}, {m['total_rounds']}, {p1_won}, {p2_won}, "
            f"{escape_str(m['match_status'])}, {m['player1_final_score']}, {m['player2_final_score']}, {dur}) "
            f"ON CONFLICT (match_id) DO NOTHING;"
        )
    lines.append("")

    # 4. Match Players
    mp_cols = [r[1] for r in cursor.execute("PRAGMA table_info(match_players)")]
    cursor.execute("SELECT * FROM match_players ORDER BY match_player_id")
    match_players = cursor.fetchall()
    print(f"[*] Exporting {len(match_players)} match_players...")
    lines.append(f"-- 4. MATCH_PLAYERS ({len(match_players)} records)")
    for mp in match_players:
        r_won = num_or_null(mp["rounds_won"] if "rounds_won" in mp_cols else 0)
        lines.append(
            f"INSERT INTO match_players (match_player_id, match_id, user_id, character_id, final_score, rounds_won, result) "
            f"VALUES ({mp['match_player_id']}, {mp['match_id']}, {mp['user_id']}, {mp['character_id']}, "
            f"{mp['final_score']}, {r_won}, {escape_str(mp['result'])}) "
            f"ON CONFLICT (match_player_id) DO NOTHING;"
        )
    lines.append("")

    # 5. Match Rounds
    mr_cols = [r[1] for r in cursor.execute("PRAGMA table_info(match_rounds)")]
    cursor.execute("SELECT * FROM match_rounds ORDER BY round_id")
    match_rounds = cursor.fetchall()
    print(f"[*] Exporting {len(match_rounds)} match_rounds...")
    lines.append(f"-- 5. MATCH_ROUNDS ({len(match_rounds)} records)")
    for mr in match_rounds:
        loser_id = num_or_null(mr["loser_user_id"] if "loser_user_id" in mr_cols else None)
        dur = num_or_null(mr["duration_seconds"] if "duration_seconds" in mr_cols else 0)
        lines.append(
            f"INSERT INTO match_rounds (round_id, match_id, round_number, winner_user_id, loser_user_id, "
            f"player1_score, player2_score, round_status, duration_seconds) "
            f"VALUES ({mr['round_id']}, {mr['match_id']}, {mr['round_number']}, "
            f"{num_or_null(mr['winner_user_id'])}, {loser_id}, {mr['player1_score']}, {mr['player2_score']}, "
            f"{escape_str(mr['round_status'])}, {dur}) "
            f"ON CONFLICT (round_id) DO NOTHING;"
        )
    lines.append("")

    # Synchronize Sequences
    lines.append("-- Synchronize all primary key sequences")
    lines.append("SELECT setval('users_user_id_seq', COALESCE((SELECT MAX(user_id) FROM users), 1));")
    lines.append("SELECT setval('characters_character_id_seq', COALESCE((SELECT MAX(character_id) FROM characters), 1));")
    lines.append("SELECT setval('matches_match_id_seq', COALESCE((SELECT MAX(match_id) FROM matches), 1));")
    lines.append("SELECT setval('match_players_match_player_id_seq', COALESCE((SELECT MAX(match_player_id) FROM match_players), 1));")
    lines.append("SELECT setval('match_rounds_round_id_seq', COALESCE((SELECT MAX(round_id) FROM match_rounds), 1));")
    lines.append("")
    lines.append("COMMIT;")

    with open(OUTPUT_SQL_PATH, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))

    print(f"[SUCCESS] Migration SQL dump successfully written to:\n  -> {OUTPUT_SQL_PATH}")
    print("[*] To apply to Supabase: paste this file into Supabase SQL Editor and click 'Run'.")
    conn.close()

if __name__ == "__main__":
    export_to_supabase_sql()
