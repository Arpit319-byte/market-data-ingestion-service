FROM eclipse-temurin:21-jre
WORKDIR /app
COPY build/libs/StockAnalyzerMarketData-0.0.1-SNAPSHOT.jar app.jar
RUN groupadd --system app && useradd --system --gid app --uid 10001 --home-dir /app app \
    && chown app:app /app/app.jar
USER app
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
