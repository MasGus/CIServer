FROM openjdk:8-jdk-alpine

# install git
RUN apk update && apk upgrade
RUN apk add git
RUN apk --update add openssh-client
RUN adduser -D testuser
USER testuser

WORKDIR /home/testuser/
COPY ./target/CIServer-1.0-SNAPSHOT.jar /home/testuser
ENTRYPOINT ["java","-jar","CIServer-1.0-SNAPSHOT.jar"]