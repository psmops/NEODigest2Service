#!/bin/bash

set -e

mkdir -p ./tmp

pushd .
cd ..
VERSION=`xpath -q -e "/project/version/text()" pom.xml`
mvn clean package shade:shade
popd

cp ../target/NEODigest2Service.jar tmp

docker build --tag=nmops36:9281/neodigest2service:$VERSION .

cat templates/t_push-image-to-psmops-docker-registry.sh | sed "s/__VERSION__/$VERSION
rm -rf ./tmp
