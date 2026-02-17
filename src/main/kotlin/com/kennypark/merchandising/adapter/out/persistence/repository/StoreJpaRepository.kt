package com.kennypark.merchandising.adapter.out.persistence.repository

import com.kennypark.merchandising.adapter.out.persistence.entity.ProductEntity
import com.kennypark.merchandising.adapter.out.persistence.entity.StoreEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StoreJpaRepository : JpaRepository<StoreEntity, String> {
}