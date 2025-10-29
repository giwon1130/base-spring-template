package com.template.platform.features.scene.application

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.template.platform.common.error.CustomException
import com.template.platform.common.error.ErrorCode
import com.template.platform.common.util.GeometryUtils
import com.template.platform.features.scene.application.dto.SceneCountResponse
import com.template.platform.features.scene.application.dto.SceneRequest
import com.template.platform.features.scene.application.dto.SceneResponse
import com.template.platform.features.scene.domain.QScene
import com.template.platform.features.scene.domain.Scene
import com.template.platform.features.scene.domain.SceneRepository
import com.template.platform.features.scene.domain.SceneStatus
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class SceneService(
    private val sceneRepository: SceneRepository,
    private val queryFactory: JPAQueryFactory
) {

    private val scenePath = QScene("scene")

    @Transactional
    fun createScene(request: SceneRequest): SceneResponse {
        if (sceneRepository.existsByNameIgnoreCase(request.name)) {
            throw CustomException(ErrorCode.INVALID_REQUEST, "이미 존재하는 Scene 이름입니다: ${request.name}")
        }

        val scene = Scene(
            name = request.name,
            imageCreatedAt = request.imageCreatedAt,
            province = request.province,
            district = request.district,
            totalSize = request.totalSize,
            cogFilePath = request.cogFilePath,
            geotransform = request.geotransform?.toTypedArray(),
            projection = request.projection,
            geodeticPolygon = GeometryUtils.polygonFromWkt(request.geodeticPolygonWkt),
            status = request.status,
            gsd = request.gsd,
            stacUrl = request.stacUrl
        )

        return SceneResponse.from(sceneRepository.save(scene))
    }

    fun getScene(sceneId: Long): SceneResponse = SceneResponse.from(getSceneEntity(sceneId))

    fun getSceneList(
        keyword: String?,
        createdStart: Instant?,
        createdEnd: Instant?,
        statuses: List<SceneStatus>?,
        pageable: Pageable
    ): Page<SceneResponse> {
        validatePeriod(createdStart, createdEnd)

        val predicates = buildList {
            add(scenePath.deletedAt.isNull)
            keyword?.let { searchCondition(it) }?.let { add(it) }
            createDateCondition(createdStart, createdEnd)?.let { add(it) }
            statusCondition(statuses)?.let { add(it) }
        }

        val results = queryFactory
            .selectFrom(scenePath)
            .where(*predicates.toTypedArray())
            .orderBy(scenePath.sceneId.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val count = queryFactory
            .select(scenePath.count())
            .from(scenePath)
            .where(*predicates.toTypedArray())
            .fetchFirst() ?: 0L

        return PageImpl(results.map(SceneResponse::from), pageable, count)
    }

    fun countScenes(createdStart: Instant, createdEnd: Instant): SceneCountResponse {
        validatePeriod(createdStart, createdEnd)
        val count = sceneRepository.countByCreatedAtBetweenAndDeletedAtIsNull(createdStart, createdEnd)
        return SceneCountResponse(count)
    }

    @Transactional
    fun updateScene(sceneId: Long, request: SceneRequest): SceneResponse {
        val scene = getSceneEntity(sceneId)

        if (!scene.name.equals(request.name, ignoreCase = true) &&
            sceneRepository.existsByNameIgnoreCase(request.name)
        ) {
            throw CustomException(ErrorCode.INVALID_REQUEST, "이미 존재하는 Scene 이름입니다: ${request.name}")
        }

        scene.updateFrom(
            name = request.name,
            imageCreatedAt = request.imageCreatedAt,
            province = request.province,
            district = request.district,
            totalSize = request.totalSize,
            cogFilePath = request.cogFilePath,
            geotransform = request.geotransform?.toTypedArray(),
            projection = request.projection,
            geodeticPolygon = GeometryUtils.polygonFromWkt(request.geodeticPolygonWkt),
            status = request.status,
            gsd = request.gsd,
            stacUrl = request.stacUrl
        )

        return SceneResponse.from(scene)
    }

    @Transactional
    fun deleteScene(sceneId: Long) {
        val scene = getSceneEntity(sceneId)
        scene.deletedAt = Instant.now()
    }

    private fun getSceneEntity(sceneId: Long): Scene =
        sceneRepository.findById(sceneId).orElseThrow {
            CustomException(ErrorCode.ENTITY_NOT_FOUND, "Scene($sceneId)을 찾을 수 없습니다.")
        }

    private fun validatePeriod(start: Instant?, end: Instant?) {
        if (start != null && end != null && start.isAfter(end)) {
            throw CustomException(ErrorCode.INVALID_REQUEST, "시작 시간이 종료 시간보다 이후일 수 없습니다.")
        }
    }

    private fun searchCondition(keyword: String): BooleanExpression {
        val like = "%${keyword.trim()}%".lowercase()
        return scenePath.sceneId.stringValue().containsIgnoreCase(like)
            .or(scenePath.name.containsIgnoreCase(like))
            .or(scenePath.province.containsIgnoreCase(like))
            .or(scenePath.district.containsIgnoreCase(like))
            .or(scenePath.stacUrl.containsIgnoreCase(like))
    }

    private fun createDateCondition(start: Instant?, end: Instant?): BooleanExpression? = when {
        start != null && end != null -> scenePath.createdAt.between(start, end)
        start != null -> scenePath.createdAt.goe(start)
        end != null -> scenePath.createdAt.loe(end)
        else -> null
    }

    private fun statusCondition(statuses: List<SceneStatus>?): BooleanExpression? =
        statuses?.takeIf { it.isNotEmpty() }?.let { scenePath.status.`in`(it) }
}
