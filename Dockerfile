FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/census-api-0.0.1-SNAPSHOT-standalone.jar /census-api/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/census-api/app.jar"]
