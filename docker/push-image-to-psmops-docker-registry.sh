#!/bin/bash

if [ "`hostname`" != "flies" ]; then
    echo
    echo "ERROR: Must be exclusively deployed from flies"
    echo
    exit 1
fi
echo "OK: Running on flies"

VERSION=`xpath -q -e "/project/version/text()" ../pom.xml`
if [ "$VERSION" == "" ]; then
    echo
    echo "ERROR: $0 <VERSION>"
    echo
    exit 1
fi
echo "OK: Version [$VERSION]"

sudo iptables -t nat -A OUTPUT -p tcp --dport 9281 -d 10.11.0.148 -j DNAT --to 127.0.0.1:9281

# Test if tunnel is active
if ! nc -zw5 nmops36 9281; then
    echo
    echo "ERROR: Tunnel is not active"
    echo
    exit 1
fi
echo "OK: Tunnel is active"

docker push nmops36:9281/neodigest2service:$VERSION

