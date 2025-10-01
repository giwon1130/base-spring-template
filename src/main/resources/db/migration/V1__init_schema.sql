-- 사용자 테이블 생성
CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    created_by BIGINT NULL,
    last_modified_by BIGINT NULL
);

-- 이메일 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- 소프트 삭제된 사용자 제외 인덱스
CREATE INDEX IF NOT EXISTS idx_users_active ON users(email) WHERE deleted_at IS NULL;

-- 기본 관리자 계정 생성
INSERT INTO users (email, password, name, role) 
VALUES ('admin@template.com', '$2a$10$T5ZzUuJpq0JmC6QZs3/M4uH5uA1G9Q0.J8HRHt4p.QzYHv2D6.BW2', 'Admin User', 'ADMIN')
ON CONFLICT (email) DO NOTHING;