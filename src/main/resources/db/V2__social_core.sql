create table posts (
                       id uuid primary key,
                       user_id uuid not null references users(id) on delete cascade,
                       content text not null,
                       image_url varchar(500),
                       created_at timestamptz not null default now(),
                       updated_at timestamptz not null default now()
);

create index idx_posts_created_at on posts(created_at desc);
create index idx_posts_user_created_at on posts(user_id, created_at desc);

create table post_likes (
                            post_id uuid not null references posts(id) on delete cascade,
                            user_id uuid not null references users(id) on delete cascade,
                            created_at timestamptz not null default now(),
                            primary key (post_id, user_id)
);

create index idx_post_likes_user_id on post_likes(user_id);

create table friend_requests (
                                 id uuid primary key,
                                 sender_id uuid not null references users(id) on delete cascade,
                                 receiver_id uuid not null references users(id) on delete cascade,
                                 status varchar(20) not null,
                                 created_at timestamptz not null default now(),
                                 updated_at timestamptz not null default now(),
                                 constraint chk_friend_request_self check (sender_id <> receiver_id)
);

create unique index uq_friend_request_pending
    on friend_requests(sender_id, receiver_id, status);

create index idx_friend_requests_receiver_status
    on friend_requests(receiver_id, status);

create table friendships (
                             user_id uuid not null references users(id) on delete cascade,
                             friend_id uuid not null references users(id) on delete cascade,
                             created_at timestamptz not null default now(),
                             primary key (user_id, friend_id),
                             constraint chk_friendship_self check (user_id <> friend_id)
);

create index idx_friendships_friend_id on friendships(friend_id);


-- Thêm cột xóa mềm cho Posts
ALTER TABLE posts ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX idx_posts_is_deleted ON posts(is_deleted);

-- Bảng Comments (Hỗ trợ nhiều cấp)
CREATE TABLE comments (
                          id UUID PRIMARY KEY,
                          post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
                          user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                          parent_id UUID REFERENCES comments(id) ON DELETE CASCADE,
                          content TEXT NOT NULL,
                          is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_comments_post_id ON comments(post_id);
CREATE INDEX idx_comments_parent_id ON comments(parent_id);
CREATE INDEX idx_posts_created_id_desc
    ON posts (created_at DESC, id DESC);
