#! /bin/sh

until (echo select 1 | ./postgres-cli.sh -i > /dev/null 2>&1)
do
 echo sleeping for postgres
 sleep 5
done
