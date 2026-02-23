package com.kennypark.merchandising.adapter.`in`.controller

import com.kennypark.merchandising.application.service.MerchandisingUseCases
import com.kennypark.merchandising.domain.ProductCachingVo
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/merchandise")
class MerchandisingController(
    val merchandisingUseCases: MerchandisingUseCases
) {
    @GetMapping("/product/inquiries")
    fun productInquiries(
        @RequestParam("store") storeCode: String? = null,
        @RequestParam("category") categoryCode: String? = null,
        @RequestParam("page") page: Int = 1,
        @RequestParam("orderType") orderType: String = "list-price",
        @RequestParam("orderSort") orderSort: String = "asc"
    ): ResponseEntity<List<ProductCachingVo?>?> {
        return ResponseEntity.ok(merchandisingUseCases.productInquiries(storeCode, categoryCode, page, 20, orderType, orderSort))
    }
}