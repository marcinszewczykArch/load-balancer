FROM hseeberger/scala-sbt:17.0.2_1.6.2_3.1.1

WORKDIR /app

COPY . .

RUN sbt clean assembly

EXPOSE 8080

CMD ["java", "-jar", "target/scala-3.6.2/load-balancer.jar"]
