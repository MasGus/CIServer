FROM ubuntu:18.04

# install git
RUN apt-get update
RUN apt-get install -y git

# add credentials on build
ARG SSH_PRIVATE_KEY
RUN mkdir /root/.ssh/
RUN echo "${ssh_prv_key}" > /root/.ssh/id_rsa

FROM openjdk:8-jdk-alpine
WORKDIR /usr/app/
COPY ./target/CIServer-1.0-SNAPSHOT.jar /usr/app
ENTRYPOINT ["java","-jar","CIServer-1.0-SNAPSHOT.jar"]



