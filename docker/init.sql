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
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    gathering_name VARCHAR(255) NOT NULL,
    member_id      BIGINT       NOT NULL,
    interval_days  BIGINT       NOT NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at     TIMESTAMP    NULL,
    last_pushed_at TIMESTAMP    NULL,

    INDEX idx_deleted_at (deleted_at),

    CONSTRAINT fk_gathering_member FOREIGN KEY (member_id) REFERENCES member (id)
);

CREATE TABLE IF NOT EXISTS gathering_member
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    gathering_id BIGINT    NOT NULL,
    member_id    BIGINT    NOT NULL,
    is_host      BIT       NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at   TIMESTAMP NULL,

    CONSTRAINT fk_gathering_member_gathering_id FOREIGN KEY (gathering_id) REFERENCES gathering (id),
    CONSTRAINT fk_gathering_member_member_id FOREIGN KEY (member_id) REFERENCES member (id),
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

    CONSTRAINT fk_invitation_gathering_id FOREIGN KEY (gathering_id) REFERENCES gathering (id),
    CONSTRAINT fk_invitation_member_id FOREIGN KEY (member_id) REFERENCES member (id),
    INDEX idx_gathering_member (gathering_id, member_id),
    INDEX idx_deleted_at (deleted_at)
);

CREATE TABLE IF NOT EXISTS category
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP    NULL,

    INDEX idx_deleted_at (deleted_at)
);

CREATE TABLE IF NOT EXISTS tag
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    category_id BIGINT       NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at  TIMESTAMP    NULL,

    CONSTRAINT fk_tag_category FOREIGN KEY (category_id) REFERENCES category (id),
    INDEX idx_deleted_at (deleted_at)
);

CREATE TABLE IF NOT EXISTS gathering_tag
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    gathering_id BIGINT NOT NULL,
    tag_id       BIGINT NOT NULL,

    CONSTRAINT fk_gathering_tag_gathering FOREIGN KEY (gathering_id) REFERENCES gathering (id),
    CONSTRAINT fk_gathering_tag_tag FOREIGN KEY (tag_id) REFERENCES tag (id),
    UNIQUE KEY uk_gathering_tag (gathering_id, tag_id)
);

CREATE TABLE IF NOT EXISTS device
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL,
    deleted_at   TIMESTAMP DEFAULT NULL,

    device_id    VARCHAR(255) NOT NULL,
    member_id    BIGINT       NOT NULL,
    is_active    BOOLEAN   DEFAULT NULL,
    device_token VARCHAR(255) NOT NULL,
    app_version  VARCHAR(20),
    os_version   VARCHAR(20),

    INDEX idx_device_device_id (device_id),
    INDEX idx_device_member_id (member_id),
    INDEX idx_device_deleted_at (deleted_at)
)