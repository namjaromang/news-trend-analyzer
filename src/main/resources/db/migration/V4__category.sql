CREATE TABLE category
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    naver_code  INT          NOT NULL,
    create_date DATETIME DEFAULT CURRENT_TIMESTAMP
);

alter table articles
    add category_id int not null after description;

INSERT INTO insight.category (title, naver_code, create_date)
VALUES ('정치', 100, DEFAULT),
       ('경제', 101, DEFAULT),
       ('사회', 102, DEFAULT),
       ('문화', 103, DEFAULT),
       ('세계', 104, DEFAULT),
       ('IT/과학', 105, DEFAULT);

