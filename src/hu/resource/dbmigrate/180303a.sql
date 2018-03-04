create table email_change (
	id int auto_increment primary key,
	user_id int not null unique,
	old_email varchar(50) not null unique,
	new_email varchar(50) not null unique,
	token varchar(32) not null,
	created timestamp not null,
	FOREIGN KEY (user_id) REFERENCES user(id)
);
