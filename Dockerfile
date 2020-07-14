FROM eed3si9n/sbt:jdk11-alpine

COPY ./target/scala-2.12/state-machine-assembly-0.1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "app.jar", "8080" ]
