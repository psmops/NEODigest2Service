#!/bin/bash

set -e

mkdir -p ./tmp

pushd .
cd ..
mvn clean package shade:shade
popd

cp ../target/NEODigest2Service.jar tmp

docker build --tag=docker.io/schastel/neodigest2service:0.0.1 .

rm -rf ./tmp
