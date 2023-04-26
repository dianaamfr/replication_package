FROM maven:3.6.3-jdk-11-slim AS build

COPY src /home/app/src
COPY pom.xml /home/app
COPY entrypoint.sh /home/app 
RUN mvn -f /home/app/pom.xml package

FROM openjdk:11

COPY --from=build /home/app/target/*.jar /app/
COPY --from=build /home/app/entrypoint.sh .
RUN ["chmod", "+x", "./entrypoint.sh"]
RUN mkdir logs

ARG visibility_logs
ARG goodput_logs

ENV VISIBILITY_LOGS $visibility_logs
ENV GOODPUT_LOGS $goodput_logs

ENTRYPOINT ["./entrypoint.sh"]