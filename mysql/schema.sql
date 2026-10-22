create database ftgo;
GRANT ALL PRIVILEGES ON ftgo.* TO 'mysqluser'@'%' WITH GRANT OPTION;

create database ftgo_consumer_service;
GRANT ALL PRIVILEGES ON ftgo_consumer_service.* TO 'mysqluser'@'%' WITH GRANT OPTION;
