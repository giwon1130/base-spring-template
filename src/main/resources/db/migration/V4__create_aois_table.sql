-- V4: AOI(Area of Interest) 테이블 생성
-- PostGIS geometry 컬럼을 사용한 공간 데이터 지원

-- aois 테이블 생성
CREATE TABLE aois (
    aoi_id BIGSERIAL PRIMARY KEY,
    code_name VARCHAR(30) NOT NULL UNIQUE,
    geometry GEOMETRY(POLYGON, 4326) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP WITHOUT TIME ZONE
);

-- 인덱스 생성
CREATE INDEX idx_aois_code_name ON aois(code_name);
CREATE INDEX idx_aois_created_at ON aois(created_at);

-- 공간 인덱스 생성 (PostGIS)
CREATE INDEX idx_aois_geometry ON aois USING GIST(geometry);

-- 소프트 삭제를 위한 부분 인덱스
CREATE INDEX idx_aois_active ON aois(aoi_id) WHERE deleted_at IS NULL;

-- code_name은 빈 문자열이 될 수 없음
ALTER TABLE aois ADD CONSTRAINT chk_aois_code_name_not_empty 
    CHECK (LENGTH(TRIM(code_name)) > 0);

-- geometry는 NULL이 될 수 없음 (테이블 정의에서 이미 NOT NULL이지만 명시적으로)
ALTER TABLE aois ADD CONSTRAINT chk_aois_geometry_not_null 
    CHECK (geometry IS NOT NULL);

-- aois 테이블에 updated_at 자동 업데이트 트리거 적용
CREATE TRIGGER tr_aois_updated_at 
    BEFORE UPDATE ON aois 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();