package com.kennypark.merchandising.adapter.out.persistence.repository

import com.kennypark.merchandising.adapter.out.persistence.entity.ProductEntity
import com.kennypark.merchandising.adapter.out.persistence.entity.StoreProductEntity
import com.kennypark.merchandising.adapter.out.persistence.entity.StoreProductEntityPK
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StoreProductJpaRepository : JpaRepository<StoreProductEntity, StoreProductEntityPK> {
    fun findByStoreProductEntityPK_StoreCode(storeCode: String): List<StoreProductEntity>?
}