package com.kennypark.merchandising.adapter.out.persistence.entity

import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Comment

@Embeddable
class StoreProductEntityPK(
    @Comment("점포코드")
    val storeCode:String,
    @Comment("상품키")
    val productKey:String,
) {
}