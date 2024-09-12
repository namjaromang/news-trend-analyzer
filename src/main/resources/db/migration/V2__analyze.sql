CREATE TABLE keyword_summary
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id       BIGINT,
    title_word       VARCHAR(255) not null,
    description_word VARCHAR(255) not null,
    year             VARCHAR(4),
    month            VARCHAR(2),
    day              VARCHAR(2),
    create_date      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_article FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE
);