create database ftgo;
GRANT ALL PRIVILEGES ON ftgo.* TO 'mysqluser'@'%' WITH GRANT OPTION;

create database ftgo_consumer;
CREATE USER 'ftgo_consumer_user'@'%' IDENTIFIED BY 'ftgo_consumer_pw';
GRANT ALL PRIVILEGES ON ftgo_consumer.* TO 'ftgo_consumer_user'@'%';
