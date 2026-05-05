# Podsumowanie Plików w Katalogu .github

## Opis
Katalog `.github` zawiera konfigurację GitHub Actions dla projektu Polymorphia, włączając workflow'y CI/CD oraz niestandardowe akcje.

## Struktura Katalogu
```
.github/
├── workflows/
│   ├── build_push.yml
│   ├── deploy_to_develop.yml
│   ├── deploy_to_env.yml
│   ├── deploy_to_staging.yml
│   ├── run_migrations.yml
│   └── run_tests.yml
└── actions/
    └── ci_setup_java/
        └── action.yml
```

## Workflow'y (workflows/)

### build_push.yml
Workflow odpowiedzialny za budowanie i wypychanie obrazów Docker do Google Cloud Artifact Registry.

```yaml
name: Build and Push Docker Image

on:
  workflow_call:
    inputs:
      environment:
        type: string
        description: 'Target environment'
        required: false
        default: 'develop'
      working-directory:
        type: string
        description: 'Working backend directory'
        required: false
        default: './polymorphia-backend'
      image-tag:
        type: string
        description: 'Additional image tag'
        required: false
    secrets:
      GCP_PROJECT_ID:
        required: true
      GCP_SA_KEY:
        required: true
      GCP_REGION:
        required: true
      ARTIFACT_REGISTRY_NAME:
        required: true
      IMAGE_NAME:
        required: true
    outputs:
      image-digest:
        description: 'Image digest'
        value: ${{ jobs.build-push.outputs.image-digest }}

jobs:
  build-push:
    name: "Build and Push"
    runs-on: ubuntu-latest
    outputs:
      image-digest: ${{ steps.build.outputs.digest }}
    permissions:
      contents: read
      id-token: write
    steps:
      - name: Checkout repository
        uses: actions/checkout@de0fac2e4500dabe0009e67214ff5f5447ce83dd

      - name: Authenticate to Google Cloud
        uses: google-github-actions/auth@7c6bc770dae815cd3e89ee6cdf493a5fab2cc093
        with:
          credentials_json: ${{ secrets.GCP_SA_KEY }}

      - name: Set up Cloud SDK
        uses: google-github-actions/setup-gcloud@aa5489c8933f4cc7a4f7d45035b3b1440c9c10db

      - name: Configure Docker for Artifact Registry
        run: gcloud auth configure-docker ${{ secrets.GCP_REGION }}-docker.pkg.dev --quiet

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@4d04d5d9486b7bd6fa91e7baf45bbb4f8b9deedd

      - name: Generate build metadata
        id: meta
        uses: docker/metadata-action@030e881283bb7a6894de51c315a6bfe6a94e05cf
        with:
          images: ${{ secrets.GCP_REGION }}-docker.pkg.dev/${{ secrets.GCP_PROJECT_ID }}/${{ secrets.ARTIFACT_REGISTRY_NAME }}/${{ secrets.IMAGE_NAME }}
          tags: |
            type=raw,value=${{ inputs.environment }}
            type=sha,format=short,prefix=${{ inputs.environment }}-
            type=raw,value=${{ inputs.image-tag }},enable=${{ inputs.image-tag != '' }}
            type=ref,event=pr,prefix=pr-
          labels: |
            org.opencontainers.image.title=Polymorphia Backend
            org.opencontainers.image.description=Backend service for Polymorphia
            environment=${{ inputs.environment }}

      - name: Build and push Docker image
        id: build
        uses: docker/build-push-action@bcafcacb16a39f128d818304e6c9c0c18556b85f
        env:
          DOCKER_BUILD_SUMMARY: false
          DOCKER_BUILD_RECORD_UPLOAD: false
        with:
          context: ${{ inputs.working-directory }}
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}
          platforms: linux/amd64
          provenance: false
          sbom: false
          cache-from: type=gha
          cache-to: type=gha,mode=max
```

### deploy_to_develop.yml
Workflow do deploymentu na środowisko develop, wyzwalany przez dodanie etykiety 'deploy-to-develop' do PR.

```yaml
name: Build and Deploy Backend to Develop

on:
  pull_request:
    types: [labeled]

permissions:
  statuses: write
  contents: read
  checks: write
  id-token: write

jobs:
  run-tests:
    name: Run Tests
    if: github.event.label.name == 'deploy-to-develop'
    uses: ./.github/workflows/run_tests.yml

  setup-environment:
    name: Setup Environment
    needs: [run-tests]
    runs-on: ubuntu-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@de0fac2e4500dabe0009e67214ff5f5447ce83dd # v4

      - name: Install PostgreSQL 17 client tools
        run: |
          sudo install -d /usr/share/postgresql-common/pgdg
          sudo curl -o /usr/share/postgresql-common/pgdg/apt.postgresql.org.asc --fail \
            https://www.postgresql.org/media/keys/ACCC4CF8.asc
          sudo sh -c 'echo "deb [signed-by=/usr/share/postgresql-common/pgdg/apt.postgresql.org.asc] \
            https://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" \
            > /etc/apt/sources.list.d/pgdg.list'
          sudo apt-get update -qq
          sudo apt-get install -y postgresql-client-17

      - name: Clean develop database
        run: |
          PGPASSWORD="${{ secrets.DEVELOP_DB_PASSWORD }}" psql \
            -h "${{ secrets.DEVELOP_VM_IP }}" \
            -p "${{ secrets.DEVELOP_DB_PORT }}" \
            -U "${{ secrets.DEVELOP_DB_USER }}" \
            -d "${{ secrets.DEVELOP_DB_NAME }}" \
            -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public; GRANT ALL ON SCHEMA public TO ${{ secrets.DEVELOP_DB_USER }};"

      - name: Copy data from staging to develop database
        run: |
          PGPASSWORD="${{ secrets.STAGING_DB_PASSWORD }}" pg_dump \
            -h "${{ secrets.STAGING_VM_IP }}" \
            -p "${{ secrets.STAGING_DB_PORT }}" \
            -U "${{ secrets.STAGING_DB_USER }}" \
            "${{ secrets.STAGING_DB_NAME }}" > staging_dump.sql

          PGPASSWORD="${{ secrets.DEVELOP_DB_PASSWORD }}" psql \
            -h "${{ secrets.DEVELOP_VM_IP }}" \
            -p "${{ secrets.DEVELOP_DB_PORT }}" \
            -U "${{ secrets.DEVELOP_DB_USER }}" \
            "${{ secrets.DEVELOP_DB_NAME }}" < staging_dump.sql

  migrate:
    name: Run Migrations
    needs: [setup-environment]
    uses: ./.github/workflows/run_migrations.yml
    secrets:
      DB_HOST: ${{ secrets.DEVELOP_VM_IP }}
      DB_PORT: ${{ secrets.DEVELOP_DB_PORT }}
      DB_NAME: ${{ secrets.DEVELOP_DB_NAME }}
      DB_USER: ${{ secrets.DEVELOP_DB_USER }}
      DB_PASSWORD: ${{ secrets.DEVELOP_DB_PASSWORD }}

  build-push:
    name: Build and Push
    needs: [setup-environment, migrate]
    uses: ./.github/workflows/build_push.yml
    with:
      environment: develop
    secrets:
      GCP_PROJECT_ID: ${{ secrets.GCP_PROJECT_ID }}
      GCP_SA_KEY: ${{ secrets.GCP_SA_KEY }}
      GCP_REGION: ${{ secrets.GCP_REGION }}
      ARTIFACT_REGISTRY_NAME: ${{ secrets.ARTIFACT_REGISTRY_NAME }}
      IMAGE_NAME: ${{ secrets.IMAGE_NAME }}

  deploy:
    name: Deploy
    needs: [build-push]
    uses: ./.github/workflows/deploy_to_env.yml
    with:
      environment: develop
      container-port: 8082
      health-check-delay: 550
    secrets:
      VM_IP: ${{ secrets.DEVELOP_VM_IP }}
      VM_USER: ${{ secrets.DEVELOP_VM_USER }}
      SSH_PRIVATE_KEY: ${{ secrets.DEVELOP_SSH_PRIVATE_KEY }}
      GCP_PROJECT_ID: ${{ secrets.GCP_PROJECT_ID }}
      GCP_SA_KEY: ${{ secrets.GCP_SA_KEY }}
      GCP_REGION: ${{ secrets.GCP_REGION }}
      ARTIFACT_REGISTRY_NAME: ${{ secrets.ARTIFACT_REGISTRY_NAME }}
      IMAGE_NAME: ${{ secrets.IMAGE_NAME }}
      HEALTH_ENDPOINT: ${{ secrets.HEALTH_ENDPOINT }}
      APP_DOMAIN: ${{ secrets.DEVELOP_APP_DOMAIN }}
```

### deploy_to_env.yml
Ogólny workflow do deploymentu na wybrane środowisko (develop, staging, production).

```yaml
name: Deploy to Environment

on:
  workflow_call:
    inputs:
      environment:
        type: string
        required: true
        description: "Target environment: develop | staging | production"
      container-name:
        type: string
        required: false
        default: "polymorphia-backend"
      container-port:
        type: number
        required: false
        default: 8080
      health-check-delay:
        type: number
        required: false
        default: 300
        description: "Max seconds to wait for health check"
    secrets:
      VM_IP:
        required: true
      VM_USER:
        required: true
      SSH_PRIVATE_KEY:
        required: true
      GCP_PROJECT_ID:
        required: true
      GCP_SA_KEY:
        required: true
      GCP_REGION:
        required: true
      ARTIFACT_REGISTRY_NAME:
        required: true
      IMAGE_NAME:
        required: true
      HEALTH_ENDPOINT:
        required: true
      APP_DOMAIN:
        required: true

jobs:
  deploy:
    name: "Deploy to ${{ inputs.environment }}"
    runs-on: ubuntu-latest
    permissions:
      contents: read
      id-token: write
    steps:
      - name: Authenticate to Google Cloud
        uses: google-github-actions/auth@7c6bc770dae815cd3e89ee6cdf493a5fab2cc093
        with:
          credentials_json: ${{ secrets.GCP_SA_KEY }}

      - name: Get access token
        id: token
        run: |
          TOKEN=$(gcloud auth print-access-token)
          echo "::add-mask::$TOKEN"
          echo "token=$TOKEN" >> "$GITHUB_OUTPUT"

      - name: Deploy container
        uses: appleboy/ssh-action@0ff4204d59e8e51228ff73bce53f80d53301dee2
        with:
          host: ${{ secrets.VM_IP }}
          username: ${{ secrets.VM_USER }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          script: |
            set -euo pipefail

            IMAGE="${{ secrets.GCP_REGION }}-docker.pkg.dev/${{ secrets.GCP_PROJECT_ID }}/${{ secrets.ARTIFACT_REGISTRY_NAME }}/${{ secrets.IMAGE_NAME }}:${{ inputs.environment }}"
            CONTAINER="${{ inputs.container-name }}-${{ inputs.environment }}"

            echo "${{ steps.token.outputs.token }}" | docker login \
              ${{ secrets.GCP_REGION }}-docker.pkg.dev --username oauth2accesstoken --password-stdin

            docker pull "$IMAGE"

            docker stop "$CONTAINER" || true
            docker rm "$CONTAINER" || true

            docker run -d \
              --name "$CONTAINER" \
              --restart unless-stopped \
              -p ${{ inputs.container-port }}:${{ inputs.container-port }} \
              -v /mnt/data/app-config-${{ inputs.environment }}:/app/config:ro \
              -v /mnt/data/static:/app/static:ro \
              "$IMAGE" \
              --spring.config.location=file:/app/config/application.properties

            docker image prune -a --force --filter "until=168h" || true

  validate:
    name: "Validate ${{ inputs.environment }}"
    needs: [deploy]
    runs-on: ubuntu-latest
    steps:
      - name: Health check
        uses: appleboy/ssh-action@0ff4204d59e8e51228ff73bce53f80d53301dee2
        with:
          host: ${{ secrets.VM_IP }}
          username: ${{ secrets.VM_USER }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          script: |
            set -uo pipefail

            CONTAINER="${{ inputs.container-name }}-${{ inputs.environment }}"
            HEALTH_URL="${{ secrets.APP_DOMAIN }}${{ secrets.HEALTH_ENDPOINT }}"
            MAX_WAIT=${{ inputs.health-check-delay }}
            INTERVAL=60

            elapsed=0

            echo "Checking: $HEALTH_URL"
            echo "Max wait: ${MAX_WAIT}s (retry every ${INTERVAL}s)"

            while [ "$elapsed" -lt "$MAX_WAIT" ]; do
              HTTP_CODE=$(curl -sSL -o /tmp/health_response.json -w "%{http_code}" "$HEALTH_URL" || echo "000")
              echo "[$elapsed/${MAX_WAIT}s] HTTP $HTTP_CODE"
              cat /tmp/health_response.json || true
              echo ""

              if [ "$HTTP_CODE" = "200" ]; then
                echo "Health check passed!"
                exit 0
              fi

              sleep "$INTERVAL"
              elapsed=$((elapsed + INTERVAL))
            done

            echo "Health check FAILED after ${MAX_WAIT}s"

            if docker ps --filter "name=$CONTAINER" --quiet | grep -q .; then
              echo "Container logs:"
              docker logs "$CONTAINER" --tail 100
            else
              echo "Container $CONTAINER is not running"
              docker ps -a
            fi

            exit 1

  report-status:
    name: "Report Status"
    needs: [validate]
    runs-on: ubuntu-latest
    if: always()
    permissions:
      statuses: write
    steps:
      - name: Set commit status
        uses: myrotvorets/set-commit-status-action@c3bb276f37397c42c5616c9dcb262fa221a278ca
        with:
          token: ${{ secrets.GITHUB_TOKEN }}
          status: ${{ needs.validate.result == 'success' && 'success' || 'failure' }}
          context: "Polymorphia Backend / Deploy to ${{ inputs.environment }}"
          description: ${{ needs.validate.result == 'success' && 'Application is healthy and running' || 'Deployment or health check failed' }}
          sha: ${{ github.event.pull_request.head.sha || github.sha }}
```

### deploy_to_staging.yml
Workflow do automatycznego deploymentu na staging po push'u na branch develop lub ręcznego wywołania.

```yaml
name: Build and Deploy Backend to Staging

on:
  push:
    branches:
      - develop
  workflow_dispatch:

permissions:
  statuses: write
  contents: read
  checks: write
  id-token: write

jobs:
  run-tests:
    name: Run Tests
    uses: ./.github/workflows/run_tests.yml

  migrate:
    name: Run Migrations
    needs: [run-tests]
    uses: ./.github/workflows/run_migrations.yml
    secrets:
      DB_HOST: ${{ secrets.STAGING_DB_HOST }}
      DB_PORT: ${{ secrets.STAGING_DB_PORT }}
      DB_NAME: ${{ secrets.STAGING_DB_NAME }}
      DB_USER: ${{ secrets.STAGING_DB_USER }}
      DB_PASSWORD: ${{ secrets.STAGING_DB_PASSWORD }}

  build-push:
    name: Build and Push
    needs: [migrate]
    uses: ./.github/workflows/build_push.yml
    with:
      environment: staging
    secrets:
      GCP_PROJECT_ID: ${{ secrets.GCP_PROJECT_ID }}
      GCP_SA_KEY: ${{ secrets.GCP_SA_KEY }}
      GCP_REGION: ${{ secrets.GCP_REGION }}
      ARTIFACT_REGISTRY_NAME: ${{ secrets.ARTIFACT_REGISTRY_NAME }}
      IMAGE_NAME: ${{ secrets.IMAGE_NAME }}

  deploy:
    name: Deploy
    needs: [build-push]
    uses: ./.github/workflows/deploy_to_env.yml
    with:
      environment: staging
      container-port: 8081
      health-check-delay: 500
    secrets:
      VM_IP: ${{ secrets.STAGING_VM_IP }}
      VM_USER: ${{ secrets.STAGING_VM_USER }}
      SSH_PRIVATE_KEY: ${{ secrets.STAGING_SSH_PRIVATE_KEY }}
      GCP_PROJECT_ID: ${{ secrets.GCP_PROJECT_ID }}
      GCP_SA_KEY: ${{ secrets.GCP_SA_KEY }}
      GCP_REGION: ${{ secrets.GCP_REGION }}
      ARTIFACT_REGISTRY_NAME: ${{ secrets.ARTIFACT_REGISTRY_NAME }}
      IMAGE_NAME: ${{ secrets.IMAGE_NAME }}
      HEALTH_ENDPOINT: ${{ secrets.HEALTH_ENDPOINT }}
      APP_DOMAIN: ${{ secrets.STAGING_APP_DOMAIN }}
```

### run_migrations.yml
Workflow do uruchamiania migracji bazy danych przy użyciu Flyway.

```yaml
name: Run Flyway Migrations

on:
  workflow_call:
    inputs:
      working-directory:
        type: string
        description: 'Working backend directory'
        required: false
        default: './polymorphia-backend'
      flyway-version:
        type: string
        description: 'Flyway CLI version'
        required: false
        default: '10.17.0'
    secrets:
      DB_HOST:
        required: true
      DB_PORT:
        required: true
      DB_NAME:
        required: true
      DB_USER:
        required: true
      DB_PASSWORD:
        required: true

jobs:
  migrate:
    name: "Run Migrations"
    runs-on: ubuntu-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@de0fac2e4500dabe0009e67214ff5f5447ce83dd # v6.0.2

      - name: Download Flyway
        run: |
          wget -qO- https://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/10.17.0/flyway-commandline-10.17.0-linux-x64.tar.gz | tar xvz
          sudo ln -s $(pwd)/flyway-10.17.0/flyway /usr/local/bin/flyway

      - name: Run Flyway migrations
        run: |
          flyway \
            -url="jdbc:postgresql://${{ secrets.DB_HOST }}:${{ secrets.DB_PORT }}/${{ secrets.DB_NAME }}" \
            -user="${{ secrets.DB_USER }}" \
            -password="${{ secrets.DB_PASSWORD }}" \
            -locations="filesystem:./polymorphia-backend/src/main/resources/db/migration" \
            -baselineOnMigrate=true \
            -baselineVersion=0 \
            migrate
```

### run_tests.yml
Workflow do uruchamiania testów backendu.

```yaml
name: Run Tests

on: 
  workflow_call:
    inputs:
      working-directory: 
        type: string
        description: 'Working backend directory'
        required: false
        default: './polymorphia-backend'
      spring-profile:
        type: string
        description: 'Spring profile to use'
        required: false
        default: 'test'
    outputs:
      test-result:
        description: 'Test execution result'
        value: ${{ jobs.aggregate-results.outputs.result }}

jobs: 
  test-backend: 
    name: "Test Backend"
    runs-on: ubuntu-latest
    permissions:
      contents: read
      checks: write

    steps: 
      - name: Checkout repository
        uses: actions/checkout@de0fac2e4500dabe0009e67214ff5f5447ce83dd # 6.0.2

      - name: Setup Java with Maven
        uses: ./.github/actions/ci_setup_java
        with:
          working-directory: ${{ inputs.working-directory }}

      - name: Run Tests
        id: test-backend
        working-directory: ${{ inputs.working-directory }}
        run: |
          mvn clean test -Dspring.profiles.active=${{ inputs.spring-profile }} -B -V
```

## Akcje (actions/)

### ci_setup_java/action.yml
Akcja do konfiguracji środowiska Java z Maven cache.

```yaml
name: Setup Java with Maven Cache

inputs: 
  java-version: 
    type: string
    description: 'Java version to use'
    required: false
    default: '21'
  working-directory: 
    type: string
    description: 'Working backend directory'
    required: false
    default: './polymorphia-backend'

runs:
  using: 'composite'

  steps: 
    - name: Setup Java
      uses: actions/setup-java@be666c2fcd27ec809703dec50e508c2fdc7f6654 # v5.2.0
      with: 
        java-version: ${{ inputs.java-version }}
        distribution: 'temurin'
        cache: 'maven'
        cache-dependency-path: '${{ inputs.working-directory }}/pom.xml'
```