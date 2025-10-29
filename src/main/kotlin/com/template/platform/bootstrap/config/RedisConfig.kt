package com.template.platform.bootstrap.config

import com.template.platform.common.cache.CacheInvalidationMessage
import com.template.platform.common.sse.NotificationSubscriber
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.serializer.StringRedisSerializer

/**
 * Redis 설정
 */
@Configuration
class RedisConfig {
    
    /**
     * Redis Template 설정
     */
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, String> {
        return RedisTemplate<String, String>().apply {
            setConnectionFactory(connectionFactory)
            keySerializer = StringRedisSerializer()
            valueSerializer = StringRedisSerializer()
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = StringRedisSerializer()
            afterPropertiesSet()
        }
    }
    
    /**
     * Redis Pub/Sub 리스너 컨테이너
     */
    @Bean
    fun redisMessageListenerContainer(
        connectionFactory: RedisConnectionFactory,
        notificationSubscriber: NotificationSubscriber,
        cacheInvalidationSubscriber: CacheInvalidationSubscriber
    ): RedisMessageListenerContainer {
        return RedisMessageListenerContainer().apply {
            setConnectionFactory(connectionFactory)
            
            // SSE 알림 채널 구독
            addMessageListener(notificationSubscriber, notificationTopic())
            
            // 캐시 무효화 채널 구독
            addMessageListener(cacheInvalidationSubscriber, ChannelTopic("platform.cache.invalidation"))
        }
    }
    
    /**
     * 알림 채널 토픽
     */
    @Bean
    fun notificationTopic(): ChannelTopic {
        return ChannelTopic("platform.notifications")
    }
    
    /**
     * 캐시 무효화 구독자
     */
    @Bean
    fun cacheInvalidationSubscriber(
        springCacheManager: org.springframework.cache.CacheManager
    ): CacheInvalidationSubscriber {
        return CacheInvalidationSubscriber(springCacheManager)
    }
}

/**
 * 캐시 무효화 메시지 구독자
 */
class CacheInvalidationSubscriber(
    private val springCacheManager: org.springframework.cache.CacheManager
) : org.springframework.data.redis.connection.MessageListener {
    
    override fun onMessage(
        message: org.springframework.data.redis.connection.Message,
        pattern: ByteArray?
    ) {
        try {
            val json = String(message.body)
            val invalidationMessage = CacheInvalidationMessage.fromJson(json)
            
            val cache = springCacheManager.getCache(invalidationMessage.cacheName)
            if (cache != null) {
                if (invalidationMessage.key != null) {
                    cache.evict(invalidationMessage.key)
                } else {
                    cache.clear()
                }
            }
        } catch (e: Exception) {
            // 로그 처리
        }
    }
}