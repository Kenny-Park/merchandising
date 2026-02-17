package com.kennypark.merchandising.application.service

import org.springframework.data.redis.core.RedisCallback
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
class CuckooFilterService(private val redisTemplate: StringRedisTemplate) {
    /**
     * 쿠쿠필터 생성
     */
    fun createFilter(filterName: String, capacity: Long) {
        // 명령어: CF.RESERVE {key} {capacity}
        redisTemplate.execute(RedisCallback { connection ->
            connection.execute("CF.RESERVE", filterName.toByteArray(), capacity.toString().toByteArray())
        })
    }
    /**
     * 아이템 추가
     */
    fun add(filterName: String, value: String): Boolean? {
        // 명령어: CF.ADD {key} {item}
        return redisTemplate.execute(RedisCallback { connection ->
            val result = connection.execute("CF.ADD", filterName.toByteArray(), value.toByteArray())
            result as? Long == 1L // 1이면 성공, 0이면 이미 존재
        })
    }
    /**
     * 존재 여부 확인
     */
    fun exists(filterName: String, value: String): Boolean {
        // 명령어: CF.EXISTS {key} {item}
        val result = redisTemplate.execute(RedisCallback { connection ->
            connection.execute("CF.EXISTS", filterName.toByteArray(), value.toByteArray())
        })
        return result == 1L
    }
    /**
     * 아이템 삭제 (쿠쿠필터의 장점!)
     */
    fun delete(filterName: String, value: String): Boolean {
        val result = redisTemplate.execute(RedisCallback { connection ->
            connection.execute("CF.DEL", filterName.toByteArray(), value.toByteArray())
        })
        return result == 1L
    }
}