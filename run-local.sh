#!/usr/bin/env bash
# Run the backend locally with secrets from .env (gitignored) exported into the environment.
# Spring resolves these into application.yml's ${...} placeholders. No secrets live in git.
set -euo pipefail
cd "$(dirname "$0")"

if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
  echo "Loaded .env (ANTHROPIC_ENABLED=${ANTHROPIC_ENABLED:-unset})"
else
  echo "No .env found — copy .env.example to .env and fill it in. Running without it."
fi

exec ./mvnw spring-boot:run "$@"
