package com.kennypark.merchandising.adapter.out.persistence.entity

import com.kennypark.merchandising.domain.StoreVo
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.Comment

@Entity
@Table(name="STORE")
@Comment("점포")
class StoreEntity(
    @Comment("상품키")
    @NotNull @Id
    val storeCode: String,

) {
    @Comment("대분류")
    lateinit var storeName: String
    fun toVo(): StoreVo {
        return StoreVo(
            storeCode = this.storeCode,
            storeName = this.storeName,
        )
    }
}
