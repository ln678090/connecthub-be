-- __init_security_schema.sql


CREATE TABLE roles (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE
);

-- Bảng Users
CREATE TABLE users (
                       id UUID PRIMARY KEY  ,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name VARCHAR(100) NOT NULL,
                       avatar_url VARCHAR(500),
                       is_enabled BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE users
    ALTER COLUMN avatar_url
        SET DEFAULT 'https://res.cloudinary.com/dayoanitt/image/upload/v1774417116/davbhywnemftongrmdwx.jpg';

ALTER TABLE users
    ADD COLUMN cover_url varchar(255)
        DEFAULT 'https://res.cloudinary.com/dayoanitt/image/upload/v1774417246/ydts7bqldo4rdl4izki8.jpg';

ALTER TABLE users
    ADD COLUMN bio text;

ALTER TABLE users
    ADD COLUMN location varchar(100);

ALTER TABLE users
    ADD COLUMN website_url varchar(255);
ALTER TABLE users
    ADD COLUMN username varchar(255);

-- Bảng trung gian User - Role (Nhiều-Nhiều)
CREATE TABLE user_roles (
                            user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
                            PRIMARY KEY (user_id, role_id)
);

-- Insert roles mặc định
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');

alter table users
drop column avatar_url;

    add COLUMN avatar_url VARCHAR(255) DEFAULT 'https://res.cloudinary.com/dayoanitt/image/upload/v1774417116/davbhywnemftongrmdwx.jpg',
ALTER TABLE users
    ADD COLUMN cover_url VARCHAR(255) DEFAULT 'https://res.cloudinary.com/dayoanitt/image/upload/v1774417246/ydts7bqldo4rdl4izki8.jpg',
    ADD COLUMN bio TEXT,
    ADD COLUMN location VARCHAR(100),
    ADD COLUMN website_url VARCHAR(255);

update users set avatar_url = 'https://res.cloudinary.com/dayoanitt/image/upload/v1774417116/davbhywnemftongrmdwx.jpg' where avatar_url is null;
update users set cover_url = 'https://res.cloudinary.com/dayoanitt/image/upload/v1774417246/ydts7bqldo4rdl4izki8.jpg' where cover_url is null;
