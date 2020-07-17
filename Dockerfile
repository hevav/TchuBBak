FROM openjdk:8
LABEL maintainer="work@hevav.dev"
COPY ./target/TchuBBak.jar /TchuBBak/TchuBBak.jar
WORKDIR /TchuBBak
CMD java -jar TchuBBak.jar