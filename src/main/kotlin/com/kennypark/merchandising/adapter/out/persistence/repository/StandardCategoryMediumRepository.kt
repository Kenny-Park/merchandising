package com.kennypark.merchandising.adapter.out.persistence.repository

import com.kennypark.merchandising.adapter.out.persistence.entity.ProductEntity
import com.kennypark.merchandising.adapter.out.persistence.entity.StandardCategoryLargeEntity
import com.kennypark.merchandising.adapter.out.persistence.entity.StandardCategoryMediumEntity
import org.springframework.stereotype.Repository

@Repository
class StandardCategoryMediumRepository(
    val standardCategoryMediumJpaRepository: StandardCategoryMediumJpaRepository
) {
    fun findByCategoryMediumKey(categoryMediumKey: String): StandardCategoryMediumEntity? {
        return standardCategoryMediumJpaRepository.findByCategoryMediumKey(categoryMediumKey)
    }
}