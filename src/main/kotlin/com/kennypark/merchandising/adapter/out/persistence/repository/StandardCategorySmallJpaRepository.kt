package com.kennypark.merchandising.adapter.out.persistence.repository

import com.kennypark.merchandising.adapter.out.persistence.entity.StandardCategoryLargeEntity
import com.kennypark.merchandising.adapter.out.persistence.entity.StandardCategorySmallEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StandardCategorySmallJpaRepository :JpaRepository<StandardCategorySmallEntity, String> {
}