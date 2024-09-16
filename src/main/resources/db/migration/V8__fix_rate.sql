UPDATE category c
SET c.`limit` = 1000
WHERE c.id in (1, 2, 3, 4, 5, 6, 7);

alter table articles
    modify original_link varchar (512) not null;

alter table articles
    modify naver_link varchar (512) not null;

alter table articles
    add publisher varchar(50) null after title;

alter table news_schedule
    add current_start_date date null;