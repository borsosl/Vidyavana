create table bookmark (
	id int auto_increment primary key,
	user_id int not null,
	name varchar(200) not null,
	follow bit not null,
	book_segment int not null,
	ordinal int not null,
	short_ref varchar(30) not null,
	last_used timestamp not null,
	FOREIGN KEY (user_id) REFERENCES user(id)
);
