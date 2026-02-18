package com.kennypark.merchandising.adapter.out.persistence.entity

import com.kennypark.merchandising.domain.StoreProductVo
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
            storeCode = this.storeProductEntityPK.storeCode,
            productCode = this.storeProductEntityPK.productKey,
            // 점상품판매가
            listPrice = this.listPrice
            // 점상품 할인가
            // 점상품 재고 ...etc
        )
    }
}