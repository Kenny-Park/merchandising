package com.kennypark.merchandising.application.service

import com.kennypark.merchandising.domain.ProductCachingVo
import org.springframework.stereotype.Service

@Service
interface MerchandisingUseCases {
    fun productInquiries(storeCode:String, categoryCode: String, page:Int, size:Int, orderType:String, orderSort:String) : List<ProductCachingVo?>?
}

class ProductInquiriesCommand(
    val storeCode:String? = null,
    val categoryCode:String? = null,
)