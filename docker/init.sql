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

CREATE TABLE IF NOT EXISTS gathering
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    gathering_name       VARCHAR(255) NOT NULL,
    member_id            BIGINT       NOT NULL,
    first_gathering_date DATE         NOT NULL,
    last_gathering_date  DATE         NOT NULL,
    interval_days        BIGINT       NOT NULL,
    tags                 JSON         NULL,
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at           TIMESTAMP    NULL,

    INDEX idx_deleted_at (deleted_at),

    CONSTRAINT fk_gathering_member FOREIGN KEY (member_id) REFERENCES member(id)
);

CREATE TABLE IF NOT EXISTS gathering_member
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    gathering_id BIGINT      NOT NULL,
    member_id    BIGINT      NOT NULL,
    is_host      BIT         NOT NULL,
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at   TIMESTAMP   NULL,

    CONSTRAINT fk_gathering_member_gathering_id FOREIGN KEY (gathering_id) REFERENCES gathering(id),
    CONSTRAINT fk_gathering_member_member_id FOREIGN KEY (member_id) REFERENCES member(id),
    INDEX idx_gathering_member (gathering_id, member_id),
    INDEX idx_deleted_at (deleted_at)
);

CREATE TABLE IF NOT EXISTS invitation
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    gathering_id BIGINT       NOT NULL,
    member_id    BIGINT       NOT NULL,
    purpose      VARCHAR(255) NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at   TIMESTAMP    NULL,

    CONSTRAINT fk_invitation_gathering_id FOREIGN KEY (gathering_id) REFERENCES gathering(id),
    CONSTRAINT fk_invitation_member_id FOREIGN KEY (member_id) REFERENCES member(id),
    INDEX idx_gathering_member (gathering_id, member_id),
    INDEX idx_deleted_at (deleted_at)
);