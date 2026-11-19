#!/usr/bin/env python3
"""
=======================================================================
 CLASH OF BYTES: E2E VERIFICATION OF SUPABASE ENGINE & REQUIREMENTS
 Tests dual auth, controls, telemetry persistence, and stats aggregation
=======================================================================
"""

import json
import urllib.request
import urllib.error
import time
import sys

BASE_URL = "http://127.0.0.1:8080"

def request_json(path, method="GET", data=None):
    url = f"{BASE_URL}{path}"
    headers = {"Content-Type": "application/json"}
    body = json.dumps(data).encode("utf-8") if data else None
    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=5) as resp:
            content = resp.read().decode("utf-8")
            return resp.status, json.loads(content) if content.strip().startswith(("{", "[")) else content
    except urllib.error.HTTPError as e:
        err_content = e.read().decode("utf-8")
        try:
            return e.code, json.loads(err_content)
        except Exception:
            return e.code, err_content
    except Exception as e:
        return 0, str(e)

def main():
    print("=======================================================================")
    print("   CLASH OF BYTES: FULL-STACK API VERIFICATION & AUDIT")
    print("=======================================================================")

    passed = 0
    total = 0

    def check(name, condition, details):
        nonlocal passed, total
        total += 1
        if condition:
            passed += 1
            print(f"  [PASS] {name:<36} | {details}")
        else:
            print(f"  [FAIL] {name:<36} | {details}")

    # 1. System Status & Database Engine
    st, res = request_json("/api/system/status")
    check("System Status & DB Diagnostics", st == 200, f"Engine: {res.get('activeDatabase')} | Supabase Configured: {res.get('supabaseConfigured')}")

    # 2. Canonical Characters Roster
    st, chars = request_json("/api/characters")
    check("Canonical Characters Roster", st == 200 and len(chars) == 6, f"Returned {len(chars)} canonical fighters (1 to 6)")

    # 3. Two Independent Player Registrations
    ts = int(time.time())
    p1_user = f"cloud_p1_{ts}"
    p2_user = f"cloud_p2_{ts}"
    st1, reg1 = request_json("/api/auth/register", "POST", {"username": p1_user, "email": f"{p1_user}@clashofbytes.io", "password": "Password@123"})
    st2, reg2 = request_json("/api/auth/register", "POST", {"username": p2_user, "email": f"{p2_user}@clashofbytes.io", "password": "Password@123"})
    p1_id = reg1.get("user", {}).get("userId") if st1 == 200 else 0
    p2_id = reg2.get("user", {}).get("userId") if st2 == 200 else 0
    p1_tok = reg1.get("token")
    p2_tok = reg2.get("token")
    check("Dual Independent Registration", st1 == 200 and st2 == 200, f"Registered P1: {p1_user} (ID: {p1_id}), P2: {p2_user} (ID: {p2_id})")

    # 4. Independent Dual Login & Tokens
    st1_log, log1 = request_json("/api/auth/login", "POST", {"username": p1_user, "password": "Password@123"})
    st2_log, log2 = request_json("/api/auth/login", "POST", {"username": p2_user, "password": "Password@123"})
    tok1 = log1.get("token")
    tok2 = log2.get("token")
    check("Simultaneous Dual Authentication", st1_log == 200 and st2_log == 200 and tok1 != tok2, f"P1 Token: {tok1[:20]}... | P2 Token: {tok2[:20]}...")

    # 5. Security: Reject Self-Match
    st_self, self_res = request_json("/api/matches", "POST", {
        "player1UserId": p1_id,
        "player2UserId": p1_id,
        "player1CharacterId": 4,
        "player2CharacterId": 3,
        "player1FinalScore": 200,
        "player2FinalScore": 150
    })
    check("Security: Reject Self-Match", st_self == 400, f"Server correctly rejected P1 == P2 match attempt with HTTP {st_self}")

    # 6. Complete Match Persistence with Full Telemetry
    match_payload = {
        "player1UserId": p1_id,
        "player2UserId": p2_id,
        "player1CharacterId": 6, # Titan Gladiator
        "player2CharacterId": 2, # Blaze Berserker
        "player1FinalScore": 245,
        "player2FinalScore": 180,
        "winnerPlayerIndex": 1,
        "player1RoundsWon": 2,
        "player2RoundsWon": 1,
        "matchDurationSeconds": 78
    }
    st_match, match_res = request_json("/api/matches", "POST", match_payload)
    new_match_id = match_res.get("matchId") if st_match == 200 else 0
    check("Full Match Telemetry Save", st_match == 200 and new_match_id > 0, f"Committed Match #{new_match_id} with round & career telemetry")

    # 7. Match Retrieval & Archive Verification
    st_get_m, m_list = request_json("/api/matches")
    target_match = next((m for m in m_list if m.get("matchId") == new_match_id), None)
    m_valid = (target_match is not None and 
               target_match.get("player1Username") == p1_user and 
               target_match.get("player2Username") == p2_user and 
               target_match.get("winnerUsername") == p1_user and
               target_match.get("loserUsername") == p2_user and
               target_match.get("player1RoundsWon") == 2 and
               target_match.get("player2RoundsWon") == 1 and
               target_match.get("matchDurationSeconds") == 78)
    check("Match Archive & Telemetry Retrieval", m_valid, f"Match #{new_match_id}: {p1_user} (245, 2 rds) vs {p2_user} (180, 1 rd), Dur=78s")

    # 8. Player Career Statistics Calculation
    st_stats1, stats1 = request_json(f"/api/players/{p1_id}/stats")
    st_stats2, stats2 = request_json(f"/api/players/{p2_id}/stats")
    p1_stats_valid = (st_stats1 == 200 and stats1.get("totalMatches") == 1 and stats1.get("wins") == 1 and stats1.get("winRate") == 100.0)
    p2_stats_valid = (st_stats2 == 200 and stats2.get("totalMatches") == 1 and stats2.get("losses") == 1 and stats2.get("winRate") == 0.0)
    check("Player 1 Career Stats Aggregation", p1_stats_valid, f"Matches: {stats1.get('totalMatches')}, Wins: {stats1.get('wins')}, WinRate: {stats1.get('winRate')}%, TotalScore: {stats1.get('totalScore')}")
    check("Player 2 Career Stats Aggregation", p2_stats_valid, f"Matches: {stats2.get('totalMatches')}, Losses: {stats2.get('losses')}, WinRate: {stats2.get('winRate')}%, TotalScore: {stats2.get('totalScore')}")

    # 9. Leaderboard Ranking
    st_lb, lb = request_json("/api/leaderboard")
    check("Global Ranked Leaderboard", st_lb == 200 and len(lb) > 0, f"Leaderboard ranked {len(lb)} players successfully")

    print("=======================================================================")
    print(f"   SUMMARY: {passed} / {total} CHECKS PASSED ({(passed/total)*100:.1f}%)")
    print("=======================================================================")

if __name__ == "__main__":
    main()
