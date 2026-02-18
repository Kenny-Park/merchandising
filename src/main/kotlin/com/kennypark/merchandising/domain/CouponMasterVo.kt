package com.kennypark.merchandising.domain

import org.hibernate.annotations.Comment
import java.time.LocalDate
import java.time.LocalDateTime

data class CouponMasterVo(
    val couponKey: String? = null,
    val customerType: Int,
    val usableMerchantType: List<Int>,
    val couponName: String,
    val couponType: String,
    val value: Long,
    val discountType: Int, // 1: amount 2: percent
    val appliedAreaType:Int,
    val maxDiscountAmountLimit: Long?, // discountType 이 2일때만 설정가능
    val minChargeLimit: Long = 0,
    val disclaimer: String,
    val couponIssueCount: Int,
    val couponAcquireStartDateTime: LocalDate,
    val couponAcquireEndDateTime: LocalDate,
    val couponUseStartDateTime: LocalDate,
    val couponUsableDays: Int,
    val reIssuedType: Int, //
    val displayYn: Int,
) {
    // 쿠폰 발행 수
    //@Comment("쿠폰 발행 수")
    var issuedCount: Long? = 0L

    companion object {

    }
}
//25-000000-0100-0000