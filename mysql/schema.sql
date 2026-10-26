create database ftgo;
GRANT ALL PRIVILEGES ON ftgo.* TO 'mysqluser'@'%' WITH GRANT OPTION;

create database ftgo_consumer;
GRANT ALL PRIVILEGES ON ftgo_consumer.* TO 'mysqluser'@'%' WITH GRANT OPTION;
