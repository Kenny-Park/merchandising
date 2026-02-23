package com.kennypark.merchandising

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EnableJpaRepositories("com.kenny.merchandising.adapter.outbound.persistence")
@EntityScan("com.kenny.merchandising.adapter.outbound.persistence")
@SpringBootApplication
class MerchandisingApplication

fun main(args: Array<String>) {
	runApplication<MerchandisingApplication>(*args)
}
