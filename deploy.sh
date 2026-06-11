#!/usr/bin/env bash
set -euo pipefail

# Avoid AWS CLI v2 checksum stream errors on large zip uploads
export AWS_REQUEST_CHECKSUM_CALCULATION="${AWS_REQUEST_CHECKSUM_CALCULATION:-when_required}"
export AWS_RESPONSE_CHECKSUM_VALIDATION="${AWS_RESPONSE_CHECKSUM_VALIDATION:-when_required}"

APPLICATION_NAME="${APPLICATION_NAME:-nationlens-be}"
ENVIRONMENT_NAME="${ENVIRONMENT_NAME:-nationlens-be-env}"
AWS_REGION="${AWS_REGION:-us-east-1}"
JAR_FILE="target/nationlens-be.jar"

_RAW_AWS_PROFILE="${AWS_PROFILE-saurav}"
PROFILE_ARGS=()
if [ -n "$_RAW_AWS_PROFILE" ]; then
  AWS_PROFILE="$_RAW_AWS_PROFILE"
  export AWS_PROFILE
  PROFILE_ARGS=(--profile "$AWS_PROFILE")
else
  unset AWS_PROFILE
fi
unset _RAW_AWS_PROFILE

VERSION_LABEL="jar-$(date +%Y%m%d-%H%M%S)-$RANDOM"

echo "Building NationLens backend..."
if command -v /usr/libexec/java_home >/dev/null 2>&1; then
  export JAVA_HOME="$(/usr/libexec/java_home -v 21 2>/dev/null || /usr/libexec/java_home 2>/dev/null || true)"
  [ -n "${JAVA_HOME:-}" ] && export PATH="$JAVA_HOME/bin:$PATH"
fi

mvn clean package -DskipTests

if [ ! -f "$JAR_FILE" ]; then
  echo "JAR not found at $JAR_FILE"
  exit 1
fi

DEPLOY_DIR="deploy-temp"
rm -rf "$DEPLOY_DIR"
mkdir "$DEPLOY_DIR"
cp "$JAR_FILE" "$DEPLOY_DIR/"
cp Procfile "$DEPLOY_DIR/"
cp -r .ebextensions "$DEPLOY_DIR/" 2>/dev/null || true

DEPLOY_ZIP="deployment-$VERSION_LABEL.zip"
(cd "$DEPLOY_DIR" && zip -r "../$DEPLOY_ZIP" . >/dev/null)

ACCOUNT_ID="$(aws sts get-caller-identity "${PROFILE_ARGS[@]}" --query Account --output text)"
S3_BUCKET="elasticbeanstalk-$AWS_REGION-$ACCOUNT_ID"
S3_KEY="$APPLICATION_NAME/$DEPLOY_ZIP"

aws s3 cp "$DEPLOY_ZIP" "s3://$S3_BUCKET/$S3_KEY" \
  --region "$AWS_REGION" \
  "${PROFILE_ARGS[@]}"

aws elasticbeanstalk create-application-version \
  --application-name "$APPLICATION_NAME" \
  --version-label "$VERSION_LABEL" \
  --source-bundle "S3Bucket=$S3_BUCKET,S3Key=$S3_KEY" \
  --region "$AWS_REGION" \
  "${PROFILE_ARGS[@]}" \
  --description "NationLens backend deploy $(date)"

for attempt in 1 2 3 4 5 6 7 8 9 10; do
  if aws elasticbeanstalk update-environment \
    --environment-name "$ENVIRONMENT_NAME" \
    --version-label "$VERSION_LABEL" \
    --region "$AWS_REGION" \
    "${PROFILE_ARGS[@]}" 2>/dev/null; then
    break
  fi
  echo "EB environment busy, retrying in 30s ($attempt/10)..."
  sleep 30
done

rm -rf "$DEPLOY_DIR" "$DEPLOY_ZIP"

echo "Deployment initiated: $VERSION_LABEL"
