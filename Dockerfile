FROM tomcat:9.0-jdk17-temurin AS build

WORKDIR /app
COPY src ./src
COPY WebContent ./WebContent

RUN javac -encoding UTF-8 \
    -cp /usr/local/tomcat/lib/servlet-api.jar \
    -d WebContent/WEB-INF/classes \
    src/Compile.java src/Run.java \
    && cd WebContent \
    && jar -cvf /tmp/JAVA-COMPILER-PROJECT.war .

FROM tomcat:9.0-jdk17-temurin

RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /tmp/JAVA-COMPILER-PROJECT.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
