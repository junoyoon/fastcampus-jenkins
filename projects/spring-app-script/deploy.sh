#!/bin/sh
chmod 755 start.sh
docker cp *.jar server1:/root/
docker cp start.sh server1:/root/
docker exec server1 /root/start.sh "$@"

docker cp *.jar server2:/root/
docker cp start.sh server2:/root/
docker exec server2 /root/start.sh "$@"
