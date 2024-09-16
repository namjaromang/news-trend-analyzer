alter table news_schedule
    add category_title varchar(50) null after id;

alter table category
    add `limit` int not null;

