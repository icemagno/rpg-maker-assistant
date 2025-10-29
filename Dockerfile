FROM openjdk:13-jdk-alpine
MAINTAINER magno.mabreu@gmail.com
COPY ./target/monitor-1.0.war /opt/lib/
RUN mkdir /monitor
COPY ./config.json /monitor
ENTRYPOINT ["java"]
ENV LANG=pt_BR.utf8 
CMD ["-jar", "/opt/lib/monitor-1.0.war"]