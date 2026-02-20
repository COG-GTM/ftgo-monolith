CREATE DATABASE IF NOT EXISTS ftgo_order_service;
CREATE DATABASE IF NOT EXISTS ftgo_consumer_service;
CREATE DATABASE IF NOT EXISTS ftgo_restaurant_service;
CREATE DATABASE IF NOT EXISTS ftgo_courier_service;

GRANT ALL PRIVILEGES ON ftgo_order_service.* TO 'ftgo'@'%';
GRANT ALL PRIVILEGES ON ftgo_consumer_service.* TO 'ftgo'@'%';
GRANT ALL PRIVILEGES ON ftgo_restaurant_service.* TO 'ftgo'@'%';
GRANT ALL PRIVILEGES ON ftgo_courier_service.* TO 'ftgo'@'%';
FLUSH PRIVILEGES;
