create table forgotten_password (
	id int auto_increment primary key,
	email varchar(32) not null,
	password varchar(50) not null,
	created timestamp not null
);
