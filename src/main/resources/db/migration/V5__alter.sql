INSERT INTO category (title, naver_code, create_date)
VALUES ('기타', 99, DEFAULT);

alter table articles
    change link original_link varchar (255) null;

alter table articles
    add naver_link varchar(255) null after original_link;