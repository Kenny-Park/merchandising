package com.kennypark.merchandising.adapter.out.redis

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.connection.stream.*
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.data.redis.stream.StreamMessageListenerContainer
import java.time.Duration

@Configuration
class RedisConfig {

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        // IP와 포트 설정
        val config = RedisStandaloneConfiguration("localhost", 6379)
        // config.password = RedisPassword.of("your_password")

        return LettuceConnectionFactory(config)
    }

    @Bean
    fun stringRedisTemplate(redisConnectionFactory: RedisConnectionFactory): StringRedisTemplate {
        val template = StringRedisTemplate()
        template.connectionFactory = redisConnectionFactory
        //template.keySerializer = StringRedisSerializer()
        //template.hashKeySerializer = StringRedisSerializer()
        return template
    }

    @Bean
    fun redissonClient(): RedissonClient {
        val config = Config()
        // Single Server 설정 (Cluster라면 config.useClusterServers() 사용)
        config.useSingleServer()
            .setAddress("redis://localhost:6379")
            // .setPassword("비밀번호가있다면설정")
            .setConnectionMinimumIdleSize(5) // 커넥션 풀 설정
            .setConnectionPoolSize(20)

        return Redisson.create(config)
    }
}