#!/usr/bin/env python3
"""
Drop ALL tables in the NationLens database so the next app boot rebuilds the
full schema from scratch via Liquibase (migration 001 onward).

This is DESTRUCTIVE — every row in every table is permanently deleted,
including Liquibase's DATABASECHANGELOG / DATABASECHANGELOGLOCK bookkeeping
(so Liquibase re-runs every changeset on the next deploy).

Usage:
    # Credentials come from env vars (never hardcode prod secrets):
    export DB_HOST=nationlens-db.czms802u6bm1.us-east-1.rds.amazonaws.com
    export DB_USER=nationlens
    export DB_PASSWORD='NationLens2026!MySQL'
    export DB_NAME=nationlens
    export DB_PORT=3306

    python3 scripts/drop_all_tables.py --yes

Requires: pymysql  (pip install pymysql)
Note: your IP must be allowed in the RDS security group's inbound 3306 rule.
"""
import os
import sys

import pymysql


def main() -> int:
    if "--yes" not in sys.argv:
        print("Refusing to run without explicit confirmation.")
        print("This DROPS ALL TABLES (irreversible). Re-run with --yes to proceed.")
        return 2

    host = os.environ.get("DB_HOST")
    user = os.environ.get("DB_USER")
    password = os.environ.get("DB_PASSWORD")
    name = os.environ.get("DB_NAME", "nationlens")
    port = int(os.environ.get("DB_PORT", "3306"))

    if not all([host, user, password]):
        print("Missing DB_HOST / DB_USER / DB_PASSWORD env vars. See header for usage.")
        return 2

    print(f"Connecting to {user}@{host}:{port}/{name} ...")
    conn = pymysql.connect(
        host=host, user=user, password=password,
        database=name, port=port, connect_timeout=10,
    )
    try:
        with conn.cursor() as cur:
            cur.execute(
                "SELECT table_name FROM information_schema.tables "
                "WHERE table_schema=%s ORDER BY table_name",
                (name,),
            )
            tables = [r[0] for r in cur.fetchall()]

            if not tables:
                print("No tables found — database is already empty.")
                return 0

            print(f"Found {len(tables)} tables. Dropping all of them:")
            for t in tables:
                print(f"  - {t}")

            cur.execute("SET FOREIGN_KEY_CHECKS = 0")
            for t in tables:
                cur.execute(f"DROP TABLE IF EXISTS `{t}`")
            cur.execute("SET FOREIGN_KEY_CHECKS = 1")
        conn.commit()
        print(f"\nDone. Dropped {len(tables)} tables.")
        print("Next deploy/restart will rebuild the schema from Liquibase migration 001.")
        return 0
    finally:
        conn.close()


if __name__ == "__main__":
    raise SystemExit(main())
