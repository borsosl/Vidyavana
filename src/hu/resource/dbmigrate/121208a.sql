create table settings (
	db_migrate varchar(10),
	created_at char(23),
	books_version varchar(10)
);

insert into settings values ('0', cast(current_timestamp() as char), '0');

create table book (
	id int primary key,
	parent_id int,
	title varchar(100) unique,
	system_priority int unique,
	user_priority int unique,
	repo_version varchar(10),
	db_version varchar(10) default '0'
);

create index bup on book(user_priority);

create table contents (
	book_id int,
	level int,
	division varchar(100),
	title varchar(200),
	book_toc_ordinal int,
	book_para_ordinal int,
	primary key(book_id, book_toc_ordinal),
	foreign key(book_id) references book(id)
);

create table para (
	book_id int,
	book_para_ordinal int,
	style int default 0,
	txt blob,
	primary key(book_id, book_para_ordinal),
	foreign key(book_id) references book(id)
);

create table word (
	id int auto_increment primary key,
	word varchar(50)
);

create index ww on word(word);

create table occur (
	word_id int,
	book_id int,
	book_para_ordinal int,
	primary key(word_id, book_id, book_para_ordinal)
	-- intentionally leaving off foreign keys on big slow table
);

create index obi on occur(book_id);
