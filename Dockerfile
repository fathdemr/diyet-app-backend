# Stage 1 — JAR'ı katmanlarına ayır
FROM amazoncorretto:23-alpine AS builder
WORKDIR /app
COPY build/libs/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# Stage 2 — minimal alpine imajı, katmanları ayrı ayrı kopyala
FROM amazoncorretto:23-alpine
WORKDIR /app

LABEL org.opencontainers.image.title="Diyet App Api" \
      org.opencontainers.image.description="Diyet App Api" \
      org.opencontainers.image.authors="Fatih Demir <fath.demmr@gmail.com>"

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

# En az değişenden en çok değişene doğru — Docker layer cache için kritik sıralama
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 5076

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "org.springframework.boot.loader.launch.JarLauncher"]