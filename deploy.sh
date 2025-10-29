#! /bin/sh

mvn clean package

docker ps -a | awk '{ print $1,$2 }' | grep magnoabreu/shipfinder3:1.0 | awk '{print $1 }' | xargs -I {} docker rm -f {}
docker rmi magnoabreu/shipfinder3:1.0
docker build --tag=magnoabreu/shipfinder3:1.0 --rm=true .

docker run --name shipfinder --hostname=shipfinder \
-v /etc/localtime:/etc/localtime:ro \
-p 8080:8080 \
-d magnoabreu/shipfinder3:1.0

docker push magnoabreu/shipfinder3:1.0

