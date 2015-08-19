FROM apopelo/java-oracle-1.7
RUN apt-get -y --fix-missing install git
RUN apt-get -y install g++
RUN apt-get -y install gcc-multilib

WORKDIR /
COPY FileGDB_API_1_3-64.tar.gz /
RUN tar xvfz FileGDB_API_1_3-64.tar.gz
ENV ESRI_FILE_GDB_HOME=/FileGDB_API
ENV JAVA_HOME=/usr/lib/jvm/java-7-oracle
WORKDIR /

RUN git clone http://github.com/frett27/jfgdb
WORKDIR /jfgdb

RUN git checkout master

RUN /bin/bash ./gradlew tasks

RUN /bin/bash ./gradlew build -x test 
RUN /bin/bash ./gradlew distZip 

