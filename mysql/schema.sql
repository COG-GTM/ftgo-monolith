create database ftgo;
GRANT ALL PRIVILEGES ON ftgo.* TO 'mysqluser'@'%' WITH GRANT OPTION;

create database ftgo_consumer;
GRANT ALL PRIVILEGES ON ftgo_consumer.* TO 'mysqluser'@'%' WITH GRANT OPTION;

create database ftgo_restaurant;
GRANT ALL PRIVILEGES ON ftgo_restaurant.* TO 'mysqluser'@'%' WITH GRANT OPTION;

create database ftgo_courier;
GRANT ALL PRIVILEGES ON ftgo_courier.* TO 'mysqluser'@'%' WITH GRANT OPTION;

create database ftgo_order;
GRANT ALL PRIVILEGES ON ftgo_order.* TO 'mysqluser'@'%' WITH GRANT OPTION;
