create table settings (
	db_migrate varchar(10)
);

insert into settings values ('0');

create table book (
	id int primary key,
	parent_id int,
	system_priority int unique,
	user_priority int unique,
	title varchar(100) unique
);

create index bup on book(user_priority);

create table contents (
	book_id int,
	book_toc_ordinal int,
	book_para_ordinal int,
	level int,
	txt varchar(200),
	primary key(book_id, book_toc_ordinal),
	foreign key(book_id) references book(id)
);

create table para (
	book_id int,
	book_para_ordinal int,
	abs_ordinal int unique,
	style int,
	txt text,
	primary key(book_id, book_para_ordinal),
	foreign key(book_id) references book(id)
);

create table word (
	word varchar(50) primary key,
	alt_id int
);

create table occur (
	word_alt_id int,
	abs_ordinal int,
	primary key(word_alt_id, abs_ordinal),
	foreign key(word_alt_id) references word(alt_id),
	foreign key(abs_ordinal) references para(abs_ordinal)
);
