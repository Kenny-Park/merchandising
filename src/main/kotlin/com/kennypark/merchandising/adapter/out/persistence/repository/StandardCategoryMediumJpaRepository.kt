package com.kennypark.merchandising.adapter.out.persistence.repository

import com.kennypark.merchandising.adapter.out.persistence.entity.StandardCategoryLargeEntity
import com.kennypark.merchandising.adapter.out.persistence.entity.StandardCategoryMediumEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StandardCategoryMediumJpaRepository :JpaRepository<StandardCategoryMediumEntity, String> {
}