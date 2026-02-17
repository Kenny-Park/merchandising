package com.kennypark.merchandising.adapter.out.persistence.entity

import com.kennypark.merchandising.domain.PackagingTypeEnum
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.Comment
import org.jetbrains.annotations.Nullable

@Entity
@Comment("상품 상세")
@Table(name="PRODUCT_DETAIL")
class ProductDetailEntity (
    @Id
    val productKey:String,

    @Comment("판매자")
    @NotNull
    val seller:String = "상품판매자",

    /*@Comment("브랜드키")
    @Nullable
    val brandKey:Long? = null,*/

){
    @Comment("상품정보")
    @Nullable
    var productDescription:String? = null

    @Comment("포장타입")
    @Enumerated(EnumType.STRING)
    @Nullable
    var packagingType: PackagingTypeEnum? = null

    @Comment("포장단위")
    @Nullable
    var unitDescription:String? = null

    @Comment("중량/용량")
    @Nullable
    var volumeDescription:String? = null

    @Comment("알러지 정보")
    @Nullable
    var allergyDescription:String? = null

    @Comment("유의사항")
    @Nullable
    var disclaimer:String? = null

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "productDisclosureKey")
    lateinit var productDisclosure:ProductDisclosureEntity

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "deliveryDescriptionKey")
    lateinit var deliveryDescriptionKey: DeliveryDescriptionEntity

    @OneToOne(mappedBy = "productDetail")
    @Comment("프로덕트 상세 정보")
    lateinit var product: ProductEntity

}