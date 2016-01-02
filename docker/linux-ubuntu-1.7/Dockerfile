FROM ubuntu:trusty

RUN apt-get update
RUN apt-get -y --fix-missing install git
RUN apt-get -y install g++
RUN apt-get -y install curl
RUN apt-get -y install gcc-multilib
RUN apt-get -y install openssl
RUN apt-get -y install s3cmd

#install Java 7 Oracle JDK
RUN mkdir -p /usr/java/default && \
    curl -Ls 'http://download.oracle.com/otn-pub/java/jdk/7u51-b13/jdk-7u51-linux-x64.tar.gz' -H 'Cookie: oraclelicense=accept-securebackup-cookie' | \
    tar --strip-components=1 -xz -C /usr/java/default/
ENV JAVA_HOME /usr/java/default/
ENV PATH $JAVA_HOME/bin:$PATH


WORKDIR /
COPY FileGDB_API_1_4-64.tar.gz /
RUN tar xvfz FileGDB_API_1_4-64.tar.gz
ENV ESRI_FILE_GDB_HOME=/FileGDB_API
WORKDIR /

RUN git clone http://github.com/frett27/jfgdb
WORKDIR /jfgdb

RUN git checkout master

RUN /bin/bash ./gradlew tasks

RUN /bin/bash ./gradlew build -x test 
RUN /bin/bash ./gradlew distZip 

COPY s3cfg s3cfg
RUN s3cmd -c s3cfg put build/distributions/* s3://jfgdb/ 

ENV TERM=dumb

CMD ["top"]
