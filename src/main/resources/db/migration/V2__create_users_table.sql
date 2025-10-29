-- 사용자 테이블 및 기본 제약조건 생성
CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    deleted_at TIMESTAMPTZ
);

COMMENT ON TABLE users IS '플랫폼 공통 사용자 계정';
COMMENT ON COLUMN users.role IS 'USER / ADMIN';
