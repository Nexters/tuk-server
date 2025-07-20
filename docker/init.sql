CREATE DATABASE IF NOT EXISTS tuk_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS member
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255),
    email       VARCHAR(255) NOT NULL,
    social_type VARCHAR(50)  NOT NULL,
    social_id   VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at  TIMESTAMP    NULL,

    UNIQUE KEY uk_social_user (social_type, social_id),
    INDEX idx_email (email),
    INDEX idx_deleted_at (deleted_at)
);