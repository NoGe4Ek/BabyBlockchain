FROM gradle:7.3.3-jdk17 as builder
USER root
WORKDIR /builder
ADD . /builder
RUN ["gradle", "clean", "MyFatJar"]

FROM openjdk:17-oracle
WORKDIR /babyblockchain
COPY --from=builder /builder/build/libs/BabyBlockchain-1.0.jar .
ENTRYPOINT ["java", "-jar", "BabyBlockchain-1.0.jar"]