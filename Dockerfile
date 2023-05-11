FROM openjdk:17
ENV SPRING_PROFILES_ACTIVE docker
VOLUME /tmp
HEALTHCHECK --interval=5m --timeout=3s CMD curl -f http://localhost:8080/actuator/health/ || exit 1
EXPOSE 8080
ARG JAR_FILE=/build/libs/pie-cloud-1.0.0.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]