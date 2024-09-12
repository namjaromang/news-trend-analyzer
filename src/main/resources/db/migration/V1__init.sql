CREATE TABLE articles
(
    id          INT AUTO_INCREMENT PRIMARY KEY, -- 각 기사의 고유 ID
    title       VARCHAR(255) NOT NULL,          -- 기사의 제목 (NULL 불가)
    link        VARCHAR(255) NULL,              -- 기사의 요약 (NULL 허용)
    description longtext     NOT NULL,          -- 기사의 URL (NULL 불가)
    pub_date    datetime     NOT NULL,
    create_date datetime     NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

alter table articles
    modify id bigint auto_increment;
