create table settings (
	db_migrate varchar(10)
);

insert into settings values ('0');

create table user (
	id int auto_increment primary key,
	admin varchar(20),
	email varchar(50) unique,
	password varchar(50),
	name varchar(50),
	reg_token varchar(32),
	access varchar(200)
);

create index email on user(email);
