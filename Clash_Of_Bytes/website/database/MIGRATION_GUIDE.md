# Clash of Bytes: Supabase PostgreSQL Database Integration & Migration Guide

This document outlines the architecture, configuration parameters, and execution procedure for connecting the **Clash of Bytes** full-stack web platform to **Supabase PostgreSQL**.

---

## 1. Architecture Overview

Clash of Bytes features a **hybrid dual-engine database architecture**:
- **Local Development / Offline Mode**: Uses high-performance local SQLite file database (`clash_of_bytes.db`) with zero external network dependencies.
- **Production / Cloud Mode**: Uses **Supabase PostgreSQL** over port `5432` (Direct Session) or port `6543` (Transaction Pooler).

```
   +--------------------------------------------------------+
   |                Clash of Bytes Web Client               |
   |              (HTML5 Canvas + REST APIs)                |
   +---------------------------+----------------------------+
                               |
                               v
   +--------------------------------------------------------+
   |             Standalone REST Server (Port 8080)         |
   |        (Java SE 27 HTTPServer + JDBC Connection)       |
   +---------------------------+----------------------------+
                               |
                +--------------+--------------+
                |                             |
       (If SUPABASE_DB_URL set)      (Fallback / Offline)
                |                             |
                v                             v
   +-------------------------+   +--------------------------+
   |   Supabase PostgreSQL   |   |   SQLite File Database   |
   |  (Cloud Managed DB)     |   |   (clash_of_bytes.db)    |
   +-------------------------+   +--------------------------+
```

---

## 2. Environment Variables Configuration

The backend dynamically detects Supabase PostgreSQL via the following environment variables:

| Variable | Description | Example Value |
| :--- | :--- | :--- |
| `SUPABASE_DB_URL` | Complete JDBC Connection URL | `jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:6543/postgres?sslmode=require` |
| `SUPABASE_DB_HOST` | Supabase Database Host | `aws-0-us-east-1.pooler.supabase.com` |
| `SUPABASE_DB_PORT` | PostgreSQL Port (Pooler: 6543, Direct: 5432) | `6543` |
| `SUPABASE_DB_NAME` | Database Name | `postgres` |
| `SUPABASE_DB_USER` | Supabase DB User | `postgres.your_project_ref` |
| `SUPABASE_DB_PASSWORD` | Supabase Database Password | `YourSecurePassword123!` |
| `SUPABASE_SSL` | SSL Requirement | `require` |

> [!SECURITY]
> Database passwords and Supabase service-role keys are loaded **strictly on the backend server**. They are never exposed in frontend JavaScript bundles, HTML attributes, or public API responses.

---

## 3. Step-by-Step Supabase Setup Procedure

### Step 1: Create a Supabase Project
1. Log in to [Supabase Console](https://supabase.com/dashboard).
2. Click **New Project** and select your preferred region.
3. Set a strong Database Password and save it.

### Step 2: Apply the Schema
1. In the Supabase Dashboard, open the **SQL Editor**.
2. Open `website/database/supabase_schema.sql` from this repository.
3. Paste the contents into the SQL Editor and click **Run**.
4. This creates:
   - `users`, `characters`, `matches`, `match_players`, `match_rounds` tables.
   - All foreign keys, cascading constraints, and performance indexes.
   - Canonical 6 fighters seeded data.
   - Row Level Security (RLS) policies for secure public reads and backend writes.

### Step 3: Migrate Existing Player & Match Data
1. Generate the latest data dump from your local SQLite database:
   ```cmd
   python website/database/migrate_sqlite_to_supabase.py
   ```
2. Open the generated file: `website/database/supabase_data_dump.sql`.
3. In the Supabase **SQL Editor**, paste the contents and click **Run**.
4. Verify that users, matches, and career telemetry are visible in the **Table Editor**.

### Step 4: Configure Backend Environment Variables
In your terminal or deployment configuration, set the Supabase variables before starting `run_website.bat`:

```powershell
$env:SUPABASE_DB_HOST = "aws-0-us-east-1.pooler.supabase.com"
$env:SUPABASE_DB_PORT = "6543"
$env:SUPABASE_DB_NAME = "postgres"
$env:SUPABASE_DB_USER = "postgres.your_project_ref"
$env:SUPABASE_DB_PASSWORD = "YourDatabasePassword"
.\run_website.bat
```

The server will automatically detect the Supabase configuration, connect via JDBC, and log:
```
[*] Database Engine: SUPABASE POSTGRESQL (aws-0-us-east-1.pooler.supabase.com:6543)
```
If credentials are not configured, it seamlessly falls back to `clash_of_bytes.db` without crashing or losing data.
