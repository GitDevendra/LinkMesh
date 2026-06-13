USE linkmesh;

CREATE TABLE IF NOT EXISTS `user` (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    email           VARCHAR(255) NOT NULL,
    google_id       VARCHAR(100) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    picture_url     VARCHAR(500) NULL,
    role            ENUM('FREE','PREMIUM') NOT NULL DEFAULT 'FREE',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_user_email (email),
    UNIQUE KEY uq_user_google_id (google_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `url` (
    id          BIGINT          NOT NULL,
    short_url   VARCHAR(16)     NOT NULL,
    long_url    TEXT            NOT NULL,
    click_count BIGINT          NOT NULL DEFAULT 0,
    user_id     BIGINT          NULL,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at  DATETIME        NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uq_url_short    (short_url),
    KEY idx_url_user_id        (user_id),
    KEY idx_url_expires_at     (expires_at),

    CONSTRAINT fk_url_user
        FOREIGN KEY (user_id)
        REFERENCES `user` (id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;