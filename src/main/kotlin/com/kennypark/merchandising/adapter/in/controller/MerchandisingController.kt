package com.kennypark.merchandising.adapter.`in`.controller

import com.kennypark.merchandising.application.service.MerchandisingUseCases
import com.kennypark.merchandising.domain.ProductCachingVo
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/merchandise")
class MerchandisingController(
    val merchandisingUseCases: MerchandisingUseCases
) {
    @GetMapping("/product/inquiries/{store}/{category}")
    fun productInquiries(@PathVariable("store") storeCode:String,
                         @PathVariable("category") categoryCode:String): ResponseEntity<List<ProductCachingVo?>?> {
        return ResponseEntity.ok(merchandisingUseCases.productInquiries(storeCode, categoryCode))
    }
}