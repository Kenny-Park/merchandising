package com.kennypark.merchandising.adapter.out.persistence.repository

import com.kennypark.merchandising.adapter.out.persistence.entity.ProductEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductJpaRepository : JpaRepository<ProductEntity, String> {
    fun findByProductKey(productKey: String): ProductEntity?
}