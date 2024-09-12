CREATE TABLE gpt_response
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    word              VARCHAR(255),
    gpt_id            VARCHAR(255),
    model             VARCHAR(255),
    content           TEXT,
    created_date      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    prompt_tokens     INT,
    completion_tokens INT,
    total_tokens      INT
);
