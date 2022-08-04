-- CREATE database `miaosha`;

USE `miaosha`;
create table t_user
(
id BIGINT(20) primary key not null auto_increment,
name varchar(16) DEFAULT NULL
);

create table goods
	(
	id BIGINT(20) primary key not null auto_increment,
	goods_name varchar(16) DEFAULT NULL,
	goods_title varchar(64) DEFAULT NULL,
	goods_img varchar(64) DEFAULT NULL,
	goods_detail LONGTEXT,
	goods_price DECIMAL(10,2) DEFAULT '0.00',
	goods_stock INT(11) DEFAULT '0'
	);


create table seckill_goods
(
id BIGINT(20) primary key not null auto_increment,
goods_id BIGINT(20)  DEFAULT NULL,
seckill_price DECIMAL(10,2) DEFAULT '0.00',
stock_count INT(11) DEFAULT NULL,
start_date datetime DEFAULT NULL,
end_date datetime DEFAULT NULL
);

create table seckill_order
(
id BIGINT(20) primary key not null auto_increment,
user_id BIGINT(20)  DEFAULT NULL,
order_id BIGINT(20)  DEFAULT NULL,
goods_id BIGINT(20)  DEFAULT NULL
);

create table order_info
(
id BIGINT(20) primary key not null auto_increment,
user_id BIGINT(20)  DEFAULT NULL,
goods_id BIGINT(20)  DEFAULT NULL,
/*收获地址*/
delivery_addr_id BIGINT(20)  DEFAULT NULL,
goods_name varchar(16) DEFAULT NULL,
goods_count INT(11)  DEFAULT '0',
goods_price decimal(10,2) DEFAULT '0.00',
order_channel TINYINT(4) DEFAULT '0',
order_status TINYINT(4) DEFAULT '0',
create_date datetime DEFAULT NULL,
pay_date datetime DEFAULT NULL
);

create table seckill_user
(
id BIGINT(20) primary key not null auto_increment,
nickname VARCHAR(255) NOT NULL,
pwd VARCHAR(32) DEFAULT NULL,
salt VARCHAR(10) DEFAULT NULL,
head VARCHAR(128) DEFAULT NULL,
register_date DATETIME DEFAULT NULL,
last_login_time DATETIME DEFAULT NULL,
login_count INT(11) DEFAULT 0
);
