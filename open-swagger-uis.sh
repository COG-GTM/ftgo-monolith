#! /bin/bash -e


for port in 8081 ; do
    if command -v xdg-open > /dev/null ; then
        xdg-open http://${DOCKER_HOST_IP:-localhost}:$port/swagger-ui.html
    else
        open http://${DOCKER_HOST_IP:-localhost}:$port/swagger-ui.html
    fi
done
