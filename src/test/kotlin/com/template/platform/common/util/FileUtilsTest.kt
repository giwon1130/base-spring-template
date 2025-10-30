package com.template.platform.common.util

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.core.io.ByteArrayResource
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class FileUtilsTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `안전한 파일 이름 생성 테스트`() {
        // When & Then
        assertThat(FileUtils.sanitizeFileName("normal_file.txt")).isEqualTo("normal_file.txt")
        assertThat(FileUtils.sanitizeFileName("file with spaces.txt")).isEqualTo("file_with_spaces.txt")
        assertThat(FileUtils.sanitizeFileName("file@#$%^&*()")).isEqualTo("file_")
        assertThat(FileUtils.sanitizeFileName("file___multiple___underscores")).isEqualTo("file_multiple_underscores")
        assertThat(FileUtils.sanitizeFileName("한글파일명.txt")).isEqualTo("_.txt")
        
        // 긴 파일명 제한
        val longName = "a".repeat(300)
        val sanitized = FileUtils.sanitizeFileName(longName)
        assertThat(sanitized.length).isLessThanOrEqualTo(255)
    }

    @Test
    fun `파일 확장자 추출 테스트`() {
        // When & Then
        assertThat(FileUtils.getFileExtension("file.txt")).isEqualTo(".txt")
        assertThat(FileUtils.getFileExtension("image.png")).isEqualTo(".png")
        assertThat(FileUtils.getFileExtension("archive.tar.gz")).isEqualTo(".gz")
        assertThat(FileUtils.getFileExtension("no-extension")).isEqualTo("")
        assertThat(FileUtils.getFileExtension("")).isEqualTo("")
        assertThat(FileUtils.getFileExtension(".hidden")).isEqualTo("")
        assertThat(FileUtils.getFileExtension("file.")).isEqualTo("")
    }

    @Test
    fun `파일 크기 포맷 테스트`() {
        // When & Then
        assertThat(FileUtils.formatFileSize(0)).isEqualTo("0.0 B")
        assertThat(FileUtils.formatFileSize(512)).isEqualTo("512.0 B")
        assertThat(FileUtils.formatFileSize(1024)).isEqualTo("1.0 KB")
        assertThat(FileUtils.formatFileSize(1536)).isEqualTo("1.5 KB")
        assertThat(FileUtils.formatFileSize(1024 * 1024)).isEqualTo("1.0 MB")
        assertThat(FileUtils.formatFileSize(1024L * 1024 * 1024)).isEqualTo("1.0 GB")
        assertThat(FileUtils.formatFileSize(1024L * 1024 * 1024 * 1024)).isEqualTo("1.0 TB")
        
        // 큰 숫자 테스트
        assertThat(FileUtils.formatFileSize(2_560_000)).isEqualTo("2.4 MB")
    }

    @Test
    fun `임시 파일 생성 테스트`() {
        // When
        val tempFile = FileUtils.createTempFile("test", ".tmp")

        // Then
        assertThat(tempFile).exists()
        assertThat(tempFile.name).startsWith("test")
        assertThat(tempFile.name).endsWith(".tmp")
        assertThat(tempFile.canRead()).isTrue
        assertThat(tempFile.canWrite()).isTrue
        
        // 정리
        tempFile.delete()
    }

    @Test
    fun `디렉토리 생성 테스트`() {
        // Given
        val newDirPath = tempDir.resolve("new-directory").toString()

        // When
        val createdPath = FileUtils.ensureDirectoryExists(newDirPath)

        // Then
        assertThat(Files.exists(createdPath)).isTrue
        assertThat(Files.isDirectory(createdPath)).isTrue
    }

    @Test
    fun `이미 존재하는 디렉토리는 그대로 반환`() {
        // Given
        val existingDir = tempDir.toString()

        // When
        val result = FileUtils.ensureDirectoryExists(existingDir)

        // Then
        assertThat(result.toString()).isEqualTo(existingDir)
        assertThat(Files.exists(result)).isTrue
    }

    @Test
    fun `파일 존재 여부 확인 테스트`() {
        // Given
        val existingFile = tempDir.resolve("existing.txt")
        Files.createFile(existingFile)

        // When & Then
        assertThat(FileUtils.fileExists(existingFile.toString())).isTrue
        assertThat(FileUtils.fileExists(tempDir.resolve("nonexistent.txt").toString())).isFalse
    }

    @Test
    fun `파일 안전 삭제 테스트`() {
        // Given
        val testFile = tempDir.resolve("test-delete.txt").toFile()
        testFile.createNewFile()
        testFile.writeText("test content")

        // When
        val deleted = FileUtils.deleteFileSafely(testFile)

        // Then
        assertThat(deleted).isTrue
        assertThat(testFile.exists()).isFalse
    }

    @Test
    fun `존재하지 않는 파일 삭제는 성공으로 간주`() {
        // Given
        val nonExistentFile = File(tempDir.toString(), "non-existent.txt")

        // When
        val result = FileUtils.deleteFileSafely(nonExistentFile)

        // Then
        assertThat(result).isTrue
    }

    @Test
    fun `Resource를 바이트 배열로 읽기 테스트`() {
        // Given
        val testData = "Hello, World!".toByteArray()
        val resource = ByteArrayResource(testData)

        // When
        val result = FileUtils.readResourceToBytes(resource)

        // Then
        assertThat(result).isEqualTo(testData)
        assertThat(String(result)).isEqualTo("Hello, World!")
    }

    @Test
    fun `스트림과 파일 삭제 통합 테스트`() {
        // Given
        val testFile = tempDir.resolve("stream-test.txt").toFile()
        testFile.writeText("Stream test content")

        // When
        val resource = FileUtils.streamAndDeleteFile(testFile)

        // Then
        assertThat(testFile.exists()).isTrue // 아직 스트림이 닫히지 않음
        
        // 스트림 사용 후 닫기
        resource.inputStream.use { stream ->
            val content = stream.readAllBytes()
            assertThat(String(content)).isEqualTo("Stream test content")
        }

        // 스트림이 닫힌 후 파일이 삭제되어야 함
        // 약간의 지연 후 확인 (파일 시스템 동기화를 위해)
        Thread.sleep(100)
        assertThat(testFile.exists()).isFalse
    }

    @Test
    fun `빈 파일 처리 테스트`() {
        // Given
        val emptyFile = tempDir.resolve("empty.txt").toFile()
        emptyFile.createNewFile()

        // When
        val resource = FileUtils.streamAndDeleteFile(emptyFile)

        // Then
        resource.inputStream.use { stream ->
            val content = stream.readAllBytes()
            assertThat(content).isEmpty()
        }

        Thread.sleep(100)
        assertThat(emptyFile.exists()).isFalse
    }

    @Test
    fun `대용량 파일 크기 포맷 테스트`() {
        // Given
        val sizes = listOf(
            5L * 1024 * 1024 * 1024 * 1024 to "5.0 TB"  // 5TB
        )

        // When & Then
        sizes.forEach { (bytes, expected) ->
            val result = FileUtils.formatFileSize(bytes)
            if (expected.contains("TB")) {
                assertThat(result).endsWith(" TB")
            } else {
                assertThat(result).isEqualTo(expected)
            }
        }
    }
}