package com.kennypark.merchandising.adapter.out.persistence.entity

import com.kennypark.merchandising.domain.ProductCachingVo
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.Comment
import org.jetbrains.annotations.Nullable
import java.time.LocalDateTime

@Entity
@Table(name="PRODUCT")
//@Comment("상품")
class ProductEntity(
    @Comment("상품키")
    @NotNull @Id
    val productKey: String,
    /*@Comment("대분류")
    val categoryLargeKey: String,
    @Comment("중분류")
    val categoryMiddleKey: String,
    @Comment("소분류")
    val categorySmallKey: String,*/
    ) {

    @Comment("정상가")
    @NotNull
    var listPrice:Long = 0L

    @Comment("판매가")
    @Nullable
    var discountedPrice:Long = 0L

    @Comment("할인률")
    @Nullable
    var discountRate:Int = 0

    @Comment("쿠폰판매가")
    @Nullable
    var couponPrice:Long = 0L

    @Comment("쿠폰할인률")
    @Nullable
    var couponDiscountRate:Int = 0

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

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "categoryLargeKey")
    lateinit var standardCategoryLarge : StandardCategoryLargeEntity

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "categoryMediumKey")
    lateinit var standardCategoryMedium : StandardCategoryMediumEntity

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "categorySmallKey")
    lateinit var standardCategorySmall : StandardCategorySmallEntity


    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "productKey")
    lateinit var productDetail:ProductDetailEntity

    lateinit var expireAt:LocalDateTime

    fun toProductCachingVo(): ProductCachingVo {
        return ProductCachingVo(
            productKey = this.productKey,
            categoryLargeKey = this.standardCategoryLarge.categoryLargeKey,
            categoryMiddleKey = this.standardCategoryMedium.categoryMediumKey,
            categorySmallKey = this.standardCategorySmall.categorySmallKey,
            categoryLargeName = this.standardCategoryLarge.categoryLargeKeyName?:"",
            categoryMiddleName = this.standardCategoryMedium.categoryMediumKeyName?:"",
            categorySmallName = this.standardCategorySmall.categorySmallKeyName?:"",

            discountedPrice = this.discountedPrice,
            discountRate = this.discountRate,
            couponPrice = this.couponPrice,
            couponDiscountRate = this.couponDiscountRate,
            productKoreanName = this.productKoreanName,
            productEnglishName = this.productEnglishName,
            displayStartDate = this.displayStartDate,
            displayEndDate = this.displayEndDate,
            tags = "",
            expireAt = this.expireAt,
        ).also {
            listPrice = it.listPrice
        }
    }
}