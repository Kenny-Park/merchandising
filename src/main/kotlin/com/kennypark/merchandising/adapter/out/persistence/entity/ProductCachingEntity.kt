package com.kennypark.merchandising.adapter.out.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.Comment
import org.jetbrains.annotations.Nullable
import java.time.LocalDateTime

// TODO: 추후 document DB로 구성하면 좋겠다.
@Entity
@Table(name="PRODUCT_CACHING")
@Comment("상품")
class ProductCachingEntity(

    @Comment("상품키")
    @NotNull @Id
    val productKey: String? = null,
    ) {
    @Comment("대분류")
    @NotNull
    lateinit var categoryLargeKey: String
    @Comment("중분류")
    @NotNull
    lateinit var categoryMiddleKey: String
    @Comment("소분류")
    @NotNull
    lateinit var categorySmallKey: String

    @Comment("대분류명")
    @NotNull
    lateinit var categoryLargeName: String
    @Comment("중분류명")
    @NotNull
    lateinit var categoryMiddleName: String
    @Comment("소분류명")
    @NotNull
    lateinit var categorySmallName: String

    @Comment("정상가")
    @NotNull
    var listPrice:Long = 0L

    @Comment("판매가")
    @Nullable
    var discountedPrice:Long = 0L

    @Comment("할인률")
    @Nullable
    var discountRate:Int = 0

    @Comment("쿠폰적용가")
    @Nullable
    var couponPrice:Long = 0L

    @Comment("쿠폰적용가")
    @Nullable
    var couponRate:Long = 0L

    @Comment("상품한글명")
    @NotNull
    lateinit var productKoreanName:String

    @Comment("상품영문명")
    @Nullable
    var productEnglishName: String? = null

    @Comment("전시시작일")
    @Nullable
    var displayStartDate:LocalDateTime? = null

    @Comment("전시종료일")
    @Nullable
    var displayEndDate:LocalDateTime? = null

    @Comment("태그 리스트")
    @Nullable
    var tags : String? = null

}