# ============================
# Diyet App - Enhanced Makefile
# Includes release-live targets
# ============================

# ==== Config ====
IMAGE_NAME     ?= fathdemr/diyet-app-backend
TAG            ?= $(shell date +%Y.%m.%d%H%M%S)
PLATFORMS      ?= linux/amd64,linux/arm64
BUILD_ARGS     ?=

# Clean tag (remove any whitespace)
TAG := $(shell printf '%s' '$(TAG)' | tr -d '[:space:]')

# ==== Tools ====
DOCKER  ?= docker
BUILDX  ?= docker buildx

# ==== Helpers ====
GIT_SHA  := $(shell git rev-parse --short HEAD 2>/dev/null || echo "nogit")
DATETAG  := $(shell date +%Y%m%d%H%M)

.PHONY: run build test clean docker-build docker-run docker-stop docker-logs release

# ==== Gradle ====
run:
	@echo "[RUN] Uygulama baslatiliyor..."
	@./gradlew bootRun

build:
	@echo "[BUILD] Proje derleniyor..."
	@./gradlew build
	@echo "[BUILD] Tamamlandi."

test:
	@echo "[TEST] Testler calistiriliyor..."
	@./gradlew test
	@echo "[TEST] Tamamlandi."

clean:
	@echo "[CLEAN] Build dosyalari temizleniyor..."
	@./gradlew clean
	@echo "[CLEAN] Tamamlandi."

# ==== Docker ====
docker-build:
	@echo "[DOCKER-BUILD] Jar olusturuluyor (testler atlanıyor)..."
	@./gradlew build -x test
	@echo "[DOCKER-BUILD] Image build ediliyor: $(IMAGE_NAME):$(TAG)"
	@$(DOCKER) build -t "$(IMAGE_NAME):$(TAG)" -t "$(IMAGE_NAME):latest" .
	@echo "[DOCKER-BUILD] Tamamlandi -> $(IMAGE_NAME):$(TAG)"

docker-run:
	@echo "[DOCKER-RUN] Container baslatiliyor: $(IMAGE_NAME):latest"
	@echo "[DOCKER-RUN] Port: 5075 | Env: .env"
	$(DOCKER) run --env-file .env -p 5076:5076 --name diyet-app "$(IMAGE_NAME):latest"

docker-stop:
	@echo "[DOCKER-STOP] Container durduruluyor: diyet-app"
	$(DOCKER) stop diyet-app && $(DOCKER) rm diyet-app
	@echo "[DOCKER-STOP] Container kaldirildi."

docker-logs:
	@echo "[DOCKER-LOGS] diyet-app loglari izleniyor (cikis: Ctrl+C)..."
	$(DOCKER) logs -f diyet-app

# ==== Release (multi-platform) ====
release: build
	@echo "[RELEASE] Basliyor: $(IMAGE_NAME):$(TAG)"
	@echo "[RELEASE] Git SHA  : $(GIT_SHA)"
	@echo "[RELEASE] Platform : $(PLATFORMS)"
	$(BUILDX) build \
	  --platform $(PLATFORMS) \
	  -t $(IMAGE_NAME):$(TAG) \
	  --push \
	  $(BUILD_ARGS) .
	@echo "[RELEASE] latest olarak etiketleniyor..."
	$(BUILDX) imagetools create \
	  -t $(IMAGE_NAME):latest \
	  $(IMAGE_NAME):$(TAG)
	@echo "[RELEASE] Tamamlandi -> $(IMAGE_NAME):$(TAG)"