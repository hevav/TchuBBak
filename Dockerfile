FROM openjdk:8
LABEL maintainer="work@hevav.dev"
COPY ./target/pfbot-1.1-shaded.jar /pfbot/pfbot.jar
WORKDIR /pfbot
CMD java -jar pfbot.jar