package com.kennypark.merchandising.adapter.out.persistence.repository

import com.kennypark.merchandising.adapter.out.persistence.entity.StoreProductEntity
import org.springframework.stereotype.Repository

@Repository
class StoreProductRepository(
    val storeProductJpaRepository: StoreProductJpaRepository
) {

    fun findByStoreCode(storeCode:String) : List<StoreProductEntity>?{
       return storeProductJpaRepository.findByStoreProductEntityPK_StoreCode(storeCode)
    }

    fun findAll() : List<StoreProductEntity>?{
        return storeProductJpaRepository.findAll()
    }
}