package com.template.platform.common.util

import mu.KotlinLogging
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * 파일 처리 관련 유틸리티
 * 
 * 주요 기능:
 * - 파일 스트리밍 (자동 삭제)
 * - 안전한 파일 경로 처리
 * - 파일 크기 계산
 * - 임시 파일 생성
 */
object FileUtils {
    private val logger = KotlinLogging.logger {}

    /**
     * 파일을 스트리밍하고 완료 시 자동으로 삭제
     * 
     * 다운로드 완료 후 임시 파일을 자동으로 정리하는 용도로 사용
     * 
     * @param file 스트리밍할 파일
     * @return InputStreamResource (스트림 종료 시 파일 자동 삭제)
     */
    fun streamAndDeleteFile(file: File): InputStreamResource {
        logger.debug { "파일 스트리밍 시작: ${file.name}" }
        
        val deletingStream = object : BufferedInputStream(FileInputStream(file), 8192) {
            override fun close() {
                try {
                    super.close()
                    if (file.exists()) {
                        val deleted = file.delete()
                        if (deleted) {
                            logger.debug { "임시 파일 삭제 완료: ${file.name}" }
                        } else {
                            logger.warn { "임시 파일 삭제 실패: ${file.name}" }
                        }
                    }
                } catch (e: Exception) {
                    logger.error(e) { "파일 정리 중 오류 발생: ${file.name}" }
                }
            }
        }

        return InputStreamResource(deletingStream)
    }

    /**
     * 안전한 파일 이름 생성 (특수 문자 제거)
     * 
     * @param fileName 원본 파일명
     * @return 안전한 파일명
     */
    fun sanitizeFileName(fileName: String): String {
        return fileName
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")  // 허용된 문자만 유지
            .replace(Regex("_{2,}"), "_")            // 연속된 언더스코어 제거
            .take(255)                               // 파일명 길이 제한
    }

    /**
     * 파일 확장자 추출
     * 
     * @param fileName 파일명
     * @return 확장자 (점 포함, 예: ".txt")
     */
    fun getFileExtension(fileName: String): String {
        val lastDotIndex = fileName.lastIndexOf('.')
        return if (lastDotIndex > 0 && lastDotIndex < fileName.length - 1) {
            fileName.substring(lastDotIndex)
        } else {
            ""
        }
    }

    /**
     * 파일 크기를 사람이 읽기 쉬운 형식으로 변환
     * 
     * @param bytes 바이트 크기
     * @return 포맷된 크기 문자열 (예: "1.5 MB")
     */
    fun formatFileSize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0

        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }

        return String.format("%.1f %s", size, units[unitIndex])
    }

    /**
     * 임시 파일 생성
     * 
     * @param prefix 파일명 접두사
     * @param suffix 파일명 접미사 (확장자 포함)
     * @return 생성된 임시 파일
     */
    fun createTempFile(prefix: String, suffix: String): File {
        return try {
            val tempFile = Files.createTempFile(prefix, suffix).toFile()
            logger.debug { "임시 파일 생성: ${tempFile.absolutePath}" }
            tempFile
        } catch (e: IOException) {
            logger.error(e) { "임시 파일 생성 실패: prefix=$prefix, suffix=$suffix" }
            throw IllegalStateException("임시 파일 생성에 실패했습니다", e)
        }
    }

    /**
     * 디렉토리 생성 (존재하지 않는 경우)
     * 
     * @param path 디렉토리 경로
     * @return 생성된 경로
     */
    fun ensureDirectoryExists(path: String): Path {
        val dirPath = Paths.get(path)
        return try {
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath)
                logger.debug { "디렉토리 생성: $path" }
            }
            dirPath
        } catch (e: IOException) {
            logger.error(e) { "디렉토리 생성 실패: $path" }
            throw IllegalStateException("디렉토리 생성에 실패했습니다: $path", e)
        }
    }

    /**
     * 파일 존재 여부 확인
     * 
     * @param filePath 파일 경로
     * @return 존재하면 true, 아니면 false
     */
    fun fileExists(filePath: String): Boolean {
        return Files.exists(Paths.get(filePath))
    }

    /**
     * 파일 삭제 (안전)
     * 
     * @param file 삭제할 파일
     * @return 삭제 성공 여부
     */
    fun deleteFileSafely(file: File): Boolean {
        return try {
            if (file.exists()) {
                val deleted = file.delete()
                if (deleted) {
                    logger.debug { "파일 삭제 완료: ${file.name}" }
                } else {
                    logger.warn { "파일 삭제 실패: ${file.name}" }
                }
                deleted
            } else {
                true // 이미 존재하지 않으므로 성공으로 간주
            }
        } catch (e: Exception) {
            logger.error(e) { "파일 삭제 중 오류: ${file.name}" }
            false
        }
    }

    /**
     * Resource를 바이트 배열로 읽기
     * 
     * @param resource Spring Resource 객체
     * @return 바이트 배열
     */
    fun readResourceToBytes(resource: Resource): ByteArray {
        return try {
            resource.inputStream.use { it.readAllBytes() }
        } catch (e: IOException) {
            logger.error(e) { "Resource 읽기 실패: ${resource.filename}" }
            throw IllegalStateException("Resource를 읽을 수 없습니다", e)
        }
    }
}