package com.kennypark.merchandising.adapter.out.persistence.repository

import com.kennypark.merchandising.adapter.out.persistence.entity.ProductEntity
import org.springframework.stereotype.Repository

@Repository
class ProductRepository(
    val productRepository: ProductJpaRepository
) {
    fun findAll() : List<ProductEntity> {
        return productRepository.findAll()
    }

    fun findByProductKey(productKey: String): ProductEntity? {
        return productRepository.findByProductKey(productKey)
    }
}