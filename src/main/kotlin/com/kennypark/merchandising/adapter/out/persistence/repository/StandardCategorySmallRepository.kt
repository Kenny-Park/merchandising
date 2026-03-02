package com.kennypark.merchandising.adapter.out.persistence.repository

import com.kennypark.merchandising.adapter.out.persistence.entity.ProductEntity
import com.kennypark.merchandising.adapter.out.persistence.entity.StandardCategorySmallEntity
import org.springframework.stereotype.Repository

@Repository
class StandardCategorySmallRepository(
    val standardCategorySmallJpaRepository: StandardCategorySmallJpaRepository
) {
    fun findByCategorySmallKey(categorySmallKey:String): StandardCategorySmallEntity? {
        return standardCategorySmallJpaRepository.findByCategorySmallKey(categorySmallKey)
    }

}