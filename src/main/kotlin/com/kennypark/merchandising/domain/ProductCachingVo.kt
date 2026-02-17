package com.kennypark.merchandising.domain

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Serializable
class ProductCachingVo (
    val productKey: String? = null,
    val categoryLargeKey: String,
    val categoryMiddleKey: String,
    val categorySmallKey: String,
    val categoryLargeName: String,
    val categoryMiddleName: String,
    val categorySmallName: String,

    val discountedPrice:Long = 0L,
    val discountRate:Int = 0,
    val couponPrice:Long = 0L,
    val couponDiscountRate:Int = 0,
    val productKoreanName:String,
    val productEnglishName: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val displayStartDate: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val displayEndDate: LocalDateTime? = null,
    val tags : String? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val expireAt:LocalDateTime? = null
) {
    var listPrice:Long = 0L
}
