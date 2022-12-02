FROM openjdk:8-jdk-alpine
EXPOSE 8000
ADD target/nottynote-db.jar nottynote-db.jar
ENTRYPOINT ["java","-jar", "/nottynote-db.jar"]