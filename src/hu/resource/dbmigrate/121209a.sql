alter table book add column exist boolean default false;
alter table settings add column books_version varchar(10);
update settings set books_version='0';
