-- V3: Scene 테이블 생성
-- PostGIS geometry 컬럼과 배열 컬럼을 포함한 복잡한 GIS 데이터 지원

-- scenes 테이블 생성
CREATE TABLE scenes (
    scene_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    image_created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    province VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL,
    total_size BIGINT NOT NULL,
    cog_file_path TEXT,
    geotransform DOUBLE PRECISION[],
    projection VARCHAR(255),
    geodetic_polygon GEOMETRY(POLYGON, 4326),
    status VARCHAR(50) NOT NULL,
    gsd DOUBLE PRECISION,
    stac_url TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP WITHOUT TIME ZONE
);

-- 인덱스 생성
CREATE INDEX idx_scenes_name ON scenes(name);
CREATE INDEX idx_scenes_status ON scenes(status);
CREATE INDEX idx_scenes_created_at ON scenes(created_at);
CREATE INDEX idx_scenes_province_district ON scenes(province, district);
CREATE INDEX idx_scenes_image_created_at ON scenes(image_created_at);

-- 공간 인덱스 생성 (PostGIS)
CREATE INDEX idx_scenes_geodetic_polygon ON scenes USING GIST(geodetic_polygon);

-- 소프트 삭제를 위한 부분 인덱스
CREATE INDEX idx_scenes_active ON scenes(scene_id) WHERE deleted_at IS NULL;

-- Scene 상태 체크 제약조건
ALTER TABLE scenes ADD CONSTRAINT chk_scenes_status 
    CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'UNKNOWN'));

-- 파일 크기는 양수여야 함
ALTER TABLE scenes ADD CONSTRAINT chk_scenes_total_size_positive 
    CHECK (total_size > 0);

-- updated_at 자동 업데이트 트리거 함수 생성 (아직 없다면)
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- scenes 테이블에 updated_at 자동 업데이트 트리거 적용
CREATE TRIGGER tr_scenes_updated_at 
    BEFORE UPDATE ON scenes 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
