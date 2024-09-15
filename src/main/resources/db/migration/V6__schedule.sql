CREATE TABLE news_schedule
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    total_requests  INT NOT NULL,
    current_start   INT         DEFAULT 1,
    total_processed INT         DEFAULT 0,
    last_processed  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    status          VARCHAR(20) DEFAULT 'IN_PROGRESS' -- IN_PROGRESS, COMPLETED, FAILED 등
);