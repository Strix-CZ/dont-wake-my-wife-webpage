#!/bin/sh

cd client &&
elm make src/Main.elm &&
elm-test &&
scp index.html simonek@server.zubarbrno.com:/var/www/dont-wake-my-wife/alarm-ui/ &&
cd .. &&
echo "Client uploaded"
