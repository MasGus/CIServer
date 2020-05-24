FROM openjdk:8-jdk-alpine

RUN apk update && apk upgrade && apk add git && apk add openssh-client
RUN adduser -D testuser
USER testuser
RUN git config --global user.email "robot@ciserver.dev"
RUN git config --global user.name "Robot CIServer"

WORKDIR /home/testuser/
COPY ./target/CIServer-1.0-SNAPSHOT.jar /home/testuser
ENTRYPOINT ["java","-jar","CIServer-1.0-SNAPSHOT.jar"]