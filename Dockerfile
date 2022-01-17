FROM openjdk:11-jdk-slim
VOLUME /tmp
ADD target/company-hierarchy-0.0.1-SNAPSHOT.jar company-hierarchy.jar
EXPOSE 8080
RUN bash -c 'touch /company-hierarchy.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/.urandom","-jar","/company-hierarchy.jar"]