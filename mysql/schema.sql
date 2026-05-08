create database if not exists ftgo_consumer;
create database if not exists ftgo_restaurant;
create database if not exists ftgo_courier;
create database if not exists ftgo_order;

GRANT ALL PRIVILEGES ON ftgo_consumer.* TO 'mysqluser'@'%' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON ftgo_restaurant.* TO 'mysqluser'@'%' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON ftgo_courier.* TO 'mysqluser'@'%' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON ftgo_order.* TO 'mysqluser'@'%' WITH GRANT OPTION;
