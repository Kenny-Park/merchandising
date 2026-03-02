package com.kennypark.merchandising.adapter.out.persistence.repository

import com.kennypark.merchandising.adapter.out.persistence.entity.ProductEntity
import com.kennypark.merchandising.adapter.out.persistence.entity.StandardCategoryLargeEntity
import org.springframework.stereotype.Repository

@Repository
class StandardCategoryLargeRepository(
    val standardCategoryLargeJpaRepository: StandardCategoryLargeJpaRepository
) {
    fun findByCategoryLargeKey(categoryLargeKey: String): StandardCategoryLargeEntity? {
        return standardCategoryLargeJpaRepository.findByCategoryLargeKey(categoryLargeKey)
    }
}