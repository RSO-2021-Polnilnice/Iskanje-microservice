FROM adoptopenjdk:15-jre-hotspot

RUN mkdir /app

WORKDIR /app

ADD ./api/target/iskanje-api-1.0.0-SNAPSHOT.jar /app

EXPOSE 8080

CMD ["java", "-jar", "iskanje-api-1.0.0-SNAPSHOT.jar"]
#ENTRYPOINT ["java", "-jar", "iskanje-api-1.0.0-SNAPSHOT.jar"]
#CMD java -jar iskanje-api-1.0.0-SNAPSHOT.jar