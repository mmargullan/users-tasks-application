CREATE TABLE if not exists photos
(
    id           INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    user_id      INTEGER,
    file_name    VARCHAR(255),
    photo_binary BYTEA,
    CONSTRAINT pk_photos PRIMARY KEY (id),
    CONSTRAINT FK_TASK_ON_USERS FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);