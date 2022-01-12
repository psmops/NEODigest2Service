#
# Run build.sh for full build
#

# That image is an ubuntu:20.04 with digest2 installed
FROM docker.io/schastel/psmops-digest2:0.19.2
MAINTAINER schastel

RUN apt-get update && apt-get upgrade -y && DEBIAN_FRONTEND="noninteractive" TZ="Pacific/Honolulu" apt-get install -y openjdk-11-jdk-headless

RUN mkdir -p /java/psneo/digest2
COPY ./tmp/NEODigest2Service.jar /java/psneo/digest2/

CMD ["java", "-jar", "/java/psneo/digest2/NEODigest2Service.jar"]
