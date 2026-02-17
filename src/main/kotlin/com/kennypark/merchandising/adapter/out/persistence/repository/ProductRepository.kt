package com.kennypark.merchandising.adapter.out.persistence.repository

import com.kennypark.merchandising.adapter.out.persistence.entity.ProductEntity

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