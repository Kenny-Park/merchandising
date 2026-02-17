package com.kennypark.merchandising.application.service

import com.kennypark.merchandising.domain.ProductCachingVo

interface MerchandisingUseCases {
    fun productInquiries(storeCode:String, categoryCode: String) : List<ProductCachingVo?>?
}

class ProductInquiriesCommand(
    val storeCode:String? = null,
    val categoryCode:String? = null,
)