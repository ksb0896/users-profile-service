FROM eclipse-temurin:17-jdk-alpine
LABEL authors="ksb09"
VOLUME /tmp
ADD target/*.jar d-user-profile.jar
ENTRYPOINT ["java","-jar","/d-user-profile.jar"]