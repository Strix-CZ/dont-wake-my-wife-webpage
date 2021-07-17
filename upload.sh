#!/bin/sh

cd client &&
elm make src/Main.elm &&
scp index.html simonek@server.zubarbrno.com:/var/www/dont-wake-my-wife/alarm-ui/ &&
echo "Client uploaded" &&
cd ../server &&
mvn install &&
cd target &&
scp server-dont-wake-my-wife-jar-with-dependencies.jar simonek@server.zubarbrno.com:/opt/server-dont-wake-my-wife/ &&
cd ../.. &&
echo "Server uploaded" &&
ssh server.zubarbrno.com "sudo systemctl restart server-dont-wake-my-wife" &&
echo "Server restarted"
