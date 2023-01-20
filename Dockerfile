FROM ubuntu:20.04
USER root
RUN apt-get update
RUN apt-get install -y maven
RUN apt-get install -y sudo
RUN apt-get install -y unzip
RUN apt-get -y install wget

RUN wget -q https://services.gradle.org/distributions/gradle-6.5.1-bin.zip \
    && unzip gradle-6.5.1-bin.zip -d /opt \
    && rm gradle-6.5.1-bin.zip


RUN apt-get install -y curl
RUN curl -sL get-comby.netlify.app -o /tmp/script.sh
RUN sudo apt-get install -y libpcre3-dev libev4
RUN wget https://repo1.maven.org/maven2/commons-cli/commons-cli/1.5.0/commons-cli-1.5.0.jar -P /usr/lib
RUN sudo bash /tmp/script.sh



RUN apt install -y git
WORKDIR /user/local/PROJECTS
RUN git clone https://github.com/maldil/keras.git ./keras-team/keras/
WORKDIR /user/local/PROJECTS/keras-team/keras/
RUN git checkout f49e66c72ea5fe337c5292ee42f61cd75bc74727

WORKDIR /user/local/

ENV GRADLE_HOME /opt/gradle-6.5.1
ENV PATH $PATH:/opt/gradle-6.5.1/bin

COPY src ./pyevolve/src
COPY m2s/* m2s/
COPY build.gradle ./pyevolve/
COPY gradlew ./pyevolve/
COPY gradlew.bat ./pyevolve/
COPY settings.gradle ./pyevolve/

RUN mvn install:install-file -Dfile=m2s/jython3-0.0.10-SNAPSHOT.jar -DgroupId=org.python -DartifactId=jython3 -Dversion=0.0.10-SNAPSHOT -Dpackaging=jar
RUN mvn install:install-file -Dfile=m2s/com.ibm.wala.cast.python.ml-0.0.1-SNAPSHOT.jar -DgroupId=com.ibm.wala -DartifactId=com.ibm.wala.cast.python.ml -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar

WORKDIR /user/local/pyevolve
RUN gradle clean build -x test

RUN apt-get install vim -y

WORKDIR /user/local

