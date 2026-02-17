package com.kennypark.merchandising.domain


class StoreVo(
    val storeCode: String,
    val storeName: String

) {
    fun toVo(): StoreVo {
        return StoreVo(
            storeCode = this.storeCode,
            storeName = this.storeName,
        )
    }
}
