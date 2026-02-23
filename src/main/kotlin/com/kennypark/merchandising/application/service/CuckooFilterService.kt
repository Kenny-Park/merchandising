package com.kennypark.merchandising.application.service

import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.codec.StringCodec
import io.lettuce.core.output.IntegerOutput
import io.lettuce.core.protocol.CommandArgs
import io.lettuce.core.protocol.CommandType
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
        val result = redisTemplate.execute(RedisCallback { connection ->
            val native = connection.nativeConnection

            if (native is StatefulRedisConnection<*, *>) {
                // 1. 코덱 설정 (String 기준)
                val codec = StringCodec.UTF8

                // 2. 명령어와 인자 구성
                val commandType = CommandType.valueOf("CF.EXISTS")
                val output = IntegerOutput(codec)
                val args = CommandArgs(codec).add(filterName).add(value)

                // 3. 실행 (핵심: native.sync()를 사용해 직접 dispatch 호출)
                (native.sync() as io.lettuce.core.api.sync.RedisCommands<String, String>)
                    .dispatch(commandType, output, args)
            } else {
                0L
            }
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