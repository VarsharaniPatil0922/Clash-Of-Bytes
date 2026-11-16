# CLASH OF BYTES: REST API SPECIFICATION
**Version:** 1.0.0  
**Base URL:** `http://localhost:8080/api`  
**Security:** Bearer Token (JWT / Secure Session Token)  
**Content-Type:** `application/json`

---

## 1. Authentication Endpoints (`/api/auth`)

### 1.1 User Registration
- **Endpoint:** `POST /api/auth/register`
- **Description:** Registers a new player account with salted cryptographic password hashing (BCrypt / SHA-256).
- **Request Body:**
```json
{
  "username": "cyber_striker",
  "email": "striker@clashofbytes.io",
  "password": "Password@123"
}
```
- **Validation Rules:**
  - `username`: 3 to 30 characters, unique across all users.
  - `email`: valid RFC 5322 email string, unique across all users.
  - `password`: minimum 6 characters.
- **Success Response (`200 OK`):**
```json
{
  "success": true,
  "message": "User registered successfully.",
  "token": "cob_token_5_4a2c918e7d8f4019a86b",
  "user": {
    "userId": 5,
    "username": "cyber_striker",
    "email": "striker@clashofbytes.io"
  }
}
```
- **Error Response (`400 Bad Request`):**
```json
{
  "success": false,
  "message": "Username 'cyber_striker' is already taken."
}
```

---

### 1.2 User Login
- **Endpoint:** `POST /api/auth/login`
- **Description:** Authenticates user credentials via username OR email address.
- **Request Body:**
```json
{
  "username": "cyber_striker",
  "password": "Password@123"
}
```
- **Success Response (`200 OK`):**
```json
{
  "success": true,
  "token": "cob_token_5_4a2c918e7d8f4019a86b",
  "user": {
    "userId": 5,
    "username": "cyber_striker",
    "email": "striker@clashofbytes.io"
  }
}
```
- **Error Response (`401 Unauthorized`):**
```json
{
  "success": false,
  "message": "Invalid username or password."
}
```

---

### 1.3 Active User Profile
- **Endpoint:** `GET /api/auth/me?userId={id}`
- **Headers:** `Authorization: Bearer <token>`
- **Success Response (`200 OK`):**
```json
{
  "userId": 1,
  "username": "cyber_champion",
  "email": "champion@clashofbytes.io"
}
```

---

## 2. Character Roster Endpoints (`/api/characters`)

### 2.1 Get All Characters
- **Endpoint:** `GET /api/characters`
- **Description:** Returns the complete roster of 6 canonical fighters and their battle attributes.
- **Success Response (`200 OK`):**
```json
[
  {
    "characterId": 1,
    "characterName": "Ironclad Warrior",
    "characterDescription": "Armored heavy vanguard equipped with crushing greatsword slashes and Titan Guard.",
    "archetype": "WarriorFighter",
    "baseHp": 115.0,
    "baseAttack": 18.0,
    "baseDefense": 6.0,
    "speed": 5.0
  },
  {
    "characterId": 2,
    "characterName": "Blaze Berserker",
    "characterDescription": "Reckless brawler trading armor defense for devastating fury damage multipliers.",
    "archetype": "BerserkerWarrior",
    "baseHp": 130.0,
    "baseAttack": 22.0,
    "baseDefense": 4.0,
    "speed": 6.5
  },
  {
    "characterId": 3,
    "characterName": "Shadow Ninja",
    "characterDescription": "Stealth assassin utilizing blinding agility, evasion frames, and shuriken strikes.",
    "archetype": "NinjaFighter",
    "baseHp": 95.0,
    "baseAttack": 16.0,
    "baseDefense": 4.0,
    "speed": 9.0
  },
  {
    "characterId": 4,
    "characterName": "Cyber Brawler",
    "characterDescription": "High-tech martial artist featuring rapid nanotech combo rushes and cyber barriers.",
    "archetype": "CyberBrawler",
    "baseHp": 110.0,
    "baseAttack": 19.0,
    "baseDefense": 3.0,
    "speed": 7.0
  },
  {
    "characterId": 5,
    "characterName": "Aura Paladin",
    "characterDescription": "Radiant holy champion channeling blessed aegis barriers and holy radiance strikes.",
    "archetype": "PaladinFighter",
    "baseHp": 120.0,
    "baseAttack": 17.0,
    "baseDefense": 7.5,
    "speed": 4.5
  },
  {
    "characterId": 6,
    "characterName": "Titan Gladiator",
    "characterDescription": "Colosseum champion master combining raw berserker fury with elemental magma strikes.",
    "archetype": "HybridGladiator",
    "baseHp": 140.0,
    "baseAttack": 24.0,
    "baseDefense": 8.0,
    "speed": 4.0
  }
]
```

---

## 3. Match Telemetry Endpoints (`/api/matches`)

### 3.1 Record Match Result
- **Endpoint:** `POST /api/matches`
- **Description:** Atomically commits match metadata, both player junction records, and individual round-level telemetry in a single transaction.
- **Request Body:**
```json
{
  "player1UserId": 1,
  "player2UserId": 2,
  "player1CharacterId": 4,
  "player2CharacterId": 3,
  "player1FinalScore": 240,
  "player2FinalScore": 180,
  "winnerPlayerIndex": 1,
  "totalRounds": 3,
  "rounds": [
    { "roundNumber": 1, "winnerPlayerIndex": 1, "p1Score": 80, "p2Score": 60 },
    { "roundNumber": 2, "winnerPlayerIndex": 2, "p1Score": 70, "p2Score": 80 },
    { "roundNumber": 3, "winnerPlayerIndex": 1, "p1Score": 90, "p2Score": 40 }
  ]
}
```
- **Success Response (`200 OK`):**
```json
{
  "success": true,
  "matchId": 12
}
```

---

### 3.2 List Recent Matches
- **Endpoint:** `GET /api/matches`
- **Success Response (`200 OK`):**
```json
[
  {
    "matchId": 12,
    "matchDate": "2026-11-16 23:45:00",
    "player1Username": "cyber_champion",
    "player2Username": "neon_striker",
    "player1Character": "Cyber Brawler",
    "player2Character": "Shadow Ninja",
    "player1Score": 240,
    "player2Score": 180,
    "winnerUsername": "cyber_champion",
    "result": "WIN",
    "totalRounds": 3
  }
]
```

---

## 4. Player & Leaderboard Endpoints

### 4.1 Player Career Statistics
- **Endpoint:** `GET /api/players/{id}/stats`
- **Success Response (`200 OK`):**
```json
{
  "userId": 1,
  "username": "cyber_champion",
  "totalMatches": 18,
  "wins": 15,
  "losses": 3,
  "draws": 0,
  "winRate": 83.33,
  "totalScore": 3950,
  "favoriteCharacter": "Cyber Brawler"
}
```

---

### 4.2 Global Leaderboard
- **Endpoint:** `GET /api/leaderboard`
- **Description:** Returns ranked player standings ordered by total wins and total score.
- **Success Response (`200 OK`):**
```json
[
  {
    "rank": 1,
    "userId": 1,
    "username": "cyber_champion",
    "matches": 18,
    "wins": 15,
    "losses": 3,
    "winRate": 83.33,
    "totalScore": 3950,
    "favoriteCharacter": "Cyber Brawler"
  },
  {
    "rank": 2,
    "userId": 3,
    "username": "shadow_master",
    "matches": 15,
    "wins": 12,
    "losses": 3,
    "winRate": 80.00,
    "totalScore": 3120,
    "favoriteCharacter": "Shadow Ninja"
  }
]
```
