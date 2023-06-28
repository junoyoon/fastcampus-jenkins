FROM azul/zulu-openjdk-centos:17-latest
ADD build/libs/demo-0.0.1-SNAPSHOT.jar demo-0.0.1-SNAPSHOT.jar
EXPOSE 8080/tcp
ENTRYPOINT ["java", "-jar", "demo-0.0.1-SNAPSHOT.jar"]
