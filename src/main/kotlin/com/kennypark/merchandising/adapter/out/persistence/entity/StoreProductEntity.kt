package com.kennypark.merchandising.adapter.out.persistence.entity

import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Comment

@Entity
class StoreProductEntity (
    @EmbeddedId
    val storeProductEntityPK: StoreProductEntityPK,
    @Comment("상품가격")
    val listPrice: Long = 0L,
) {
    fun toVo(): StoreProductVo {
        return StoreProductVo(

        )
    }
}