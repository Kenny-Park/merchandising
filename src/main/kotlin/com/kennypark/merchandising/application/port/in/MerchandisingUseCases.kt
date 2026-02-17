package com.kennypark.merchandising.application.port.`in`

import com.kennypark.merchandising.domain.*
import org.springframework.stereotype.Service

@Service
interface MerchandisingUseCases  {
    // 상품 조회
    fun productInquiries() : List<ProductVo>?
    // 카테고리 조회
    fun categoryInquiries() : List<CategoryVo>?
    // 태그조회
    fun tagInquiries() : List<TagVo>?
    // 즉시할인정보
    fun discountInquiries() : List<DiscountVo>
    // 쿠폰정보
    fun couponInquiries() : List<CouponVo>
}