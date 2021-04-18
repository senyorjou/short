FROM clojure:lein

WORKDIR /app

COPY project.clj .

RUN lein deps

COPY . .

RUN lein ring uberjar

CMD ["java", "-jar", "target/short-0.1.0-SNAPSHOT-standalone.jar"]
