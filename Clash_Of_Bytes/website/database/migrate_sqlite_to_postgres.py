#!/usr/bin/env python3
"""
Clash of Bytes: SQLite to PostgreSQL Migration Tool
Transfers users, characters, matches, match_players, and match_rounds
from the desktop SQLite database (clash_of_bytes.db) into PostgreSQL.
"""

import sqlite3
import sys
import os

SQLITE_DB = os.path.join(os.path.dirname(os.path.abspath(__file__)), "../../clash_of_bytes.db")
OUTPUT_SQL = os.path.join(os.path.dirname(os.path.abspath(__file__)), "migrated_from_sqlite.sql")

def migrate():
    if not os.path.exists(SQLITE_DB):
        print(f"[!] SQLite DB not found at: {SQLITE_DB}")
        return

    print(f"[*] Connecting to SQLite DB: {SQLITE_DB}")
    conn = sqlite3.connect(SQLITE_DB)
    cursor = conn.cursor()

    lines = [
        "-- Auto-generated SQLite to PostgreSQL Data Dump",
        "-- Clash of Bytes Migration Tool\n",
        "BEGIN TRANSACTION;\n"
    ]

    # 1. Users
    cursor.execute("SELECT user_id, username, email, password_hash, salt, created_at FROM users")
    users = cursor.fetchall()
    print(f"[*] Found {len(users)} users.")
    for u in users:
        u_id, u_name, u_email, u_hash, u_salt, u_created = u
        u_name = u_name.replace("'", "''")
        u_email = u_email.replace("'", "''")
        lines.append(f"INSERT INTO users (user_id, username, email, password_hash, salt, created_at) "
                     f"VALUES ({u_id}, '{u_name}', '{u_email}', '{u_hash}', '{u_salt}', '{u_created}') "
                     f"ON CONFLICT (user_id) DO NOTHING;")

    # 2. Characters
    cursor.execute("SELECT character_id, character_name, character_description, archetype, base_hp, base_attack, base_defense, speed FROM characters")
    chars = cursor.fetchall()
    print(f"[*] Found {len(chars)} characters.")
    for c in chars:
        c_id, c_name, c_desc, c_arch, c_hp, c_atk, c_def, c_spd = c
        c_desc = c_desc.replace("'", "''")
        lines.append(f"INSERT INTO characters (character_id, character_name, character_description, archetype, base_hp, base_attack, base_defense, speed) "
                     f"VALUES ({c_id}, '{c_name}', '{c_desc}', '{c_arch}', {c_hp}, {c_atk}, {c_def}, {c_spd}) "
                     f"ON CONFLICT (character_id) DO NOTHING;")

    # 3. Matches
    cursor.execute("SELECT match_id, match_date, player1_user_id, player2_user_id, winner_user_id, total_rounds, match_status, player1_final_score, player2_final_score FROM matches")
    matches = cursor.fetchall()
    print(f"[*] Found {len(matches)} matches.")
    for m in matches:
        m_id, m_date, p1, p2, win, rounds, status, s1, s2 = m
        win_val = str(win) if win is not None else "NULL"
        lines.append(f"INSERT INTO matches (match_id, match_date, player1_user_id, player2_user_id, winner_user_id, total_rounds, match_status, player1_final_score, player2_final_score) "
                     f"VALUES ({m_id}, '{m_date}', {p1}, {p2}, {win_val}, {rounds}, '{status}', {s1}, {s2}) "
                     f"ON CONFLICT (match_id) DO NOTHING;")

    # 4. Match Players
    cursor.execute("SELECT match_player_id, match_id, user_id, character_id, final_score, result FROM match_players")
    mps = cursor.fetchall()
    print(f"[*] Found {len(mps)} match_players.")
    for mp in mps:
        mp_id, m_id, u_id, c_id, score, res = mp
        lines.append(f"INSERT INTO match_players (match_player_id, match_id, user_id, character_id, final_score, result) "
                     f"VALUES ({mp_id}, {m_id}, {u_id}, {c_id}, {score}, '{res}') "
                     f"ON CONFLICT DO NOTHING;")

    # 5. Match Rounds
    cursor.execute("SELECT round_id, match_id, round_number, winner_user_id, player1_score, player2_score, round_status FROM match_rounds")
    mrs = cursor.fetchall()
    print(f"[*] Found {len(mrs)} match_rounds.")
    for mr in mrs:
        r_id, m_id, r_num, win, s1, s2, status = mr
        win_val = str(win) if win is not None else "NULL"
        lines.append(f"INSERT INTO match_rounds (round_id, match_id, round_number, winner_user_id, player1_score, player2_score, round_status) "
                     f"VALUES ({r_id}, {m_id}, {r_num}, {win_val}, {s1}, {s2}, '{status}') "
                     f"ON CONFLICT DO NOTHING;")

    lines.append("\n-- Update sequences")
    lines.append("SELECT setval('users_user_id_seq', COALESCE((SELECT MAX(user_id) FROM users), 1));")
    lines.append("SELECT setval('characters_character_id_seq', COALESCE((SELECT MAX(character_id) FROM characters), 1));")
    lines.append("SELECT setval('matches_match_id_seq', COALESCE((SELECT MAX(match_id) FROM matches), 1));")
    lines.append("SELECT setval('match_players_match_player_id_seq', COALESCE((SELECT MAX(match_player_id) FROM match_players), 1));")
    lines.append("SELECT setval('match_rounds_round_id_seq', COALESCE((SELECT MAX(round_id) FROM match_rounds), 1));")
    lines.append("\nCOMMIT;\n")

    conn.close()

    with open(OUTPUT_SQL, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))

    print(f"[SUCCESS] Migration SQL script written to: {OUTPUT_SQL}")

if __name__ == "__main__":
    migrate()
