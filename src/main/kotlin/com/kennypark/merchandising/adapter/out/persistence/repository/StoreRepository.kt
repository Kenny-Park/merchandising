package com.kennypark.merchandising.adapter.out.persistence.repository

import com.kennypark.merchandising.adapter.out.persistence.entity.StoreEntity
import com.kennypark.merchandising.adapter.out.persistence.entity.StoreProductEntity
import org.springframework.stereotype.Repository

@Repository
class StoreRepository(
    val storeJpaRepository: StoreJpaRepository
) {

    fun findAll() : List<StoreEntity>?{
        return storeJpaRepository.findAll()
    }
}