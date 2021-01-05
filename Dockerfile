# Build and install dependencies with maven.
FROM maven:3-jdk-8 as build

WORKDIR /app

# Now add the java code
ADD . /app/

# The maven install rule (in pom.xml) copies all dependent jar files into the target directory.
# Mvn clean will delete all temporary .class files from the build directory.  This makes the final image smaller.
RUN mkdir appdir && \
    mvn install && \
    cp demo/target/*.jar appdir && \
    mvn clean

# This is a multi stage docker build.  Maven is not needed once the build is complete.
# Copy generated jar files into a working directory and run with a java jre.
FROM openjdk:8

WORKDIR /app

COPY --from=build /app/appdir/*.jar /app/

# display contents back to the the docker host machine
ENV DISPLAY=host.docker.internal:0

# The following extras seem to be needed to run as an X11 app
RUN apt-get update -y \
   && apt-get install -y curl \
   && apt-get install -y gnupg \
   && apt-get install -y libxext6 \
   && apt-get install -y libxrender-dev libxtst6 libfreetype6  \
   && apt-get install -y bash

RUN mkdir /data \
    && cd /data \
    && git clone https://github.com/terrywbrady/File-Analyzer-Test-Data

# Prerequisites
#  - in XQuartz, enable network connections (restart)
#  - xhost + localhost
#
# Note "xhost +" allows connections from any host
#   "xhost -" will undo that access

# The maven build bundles all dependency names into the manifest of the generated jar file.
# Run "ls /tmp" to see the set of runtime jars that are included.

CMD [ "java", "-jar", "DemoFileAnalyzer-2.0.jar" ] 