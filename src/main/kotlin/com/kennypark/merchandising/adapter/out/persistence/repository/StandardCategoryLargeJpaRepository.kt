package com.kennypark.merchandising.adapter.out.persistence.repository

import com.kennypark.merchandising.adapter.out.persistence.entity.StandardCategoryLargeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StandardCategoryLargeJpaRepository :JpaRepository<StandardCategoryLargeEntity, String> {
}