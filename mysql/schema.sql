create database ftgo;
GRANT ALL PRIVILEGES ON ftgo.* TO 'mysqluser'@'%' WITH GRANT OPTION;

CREATE DATABASE IF NOT EXISTS ftgo_courier;
GRANT ALL PRIVILEGES ON ftgo_courier.* TO 'mysqluser'@'%' WITH GRANT OPTION;
