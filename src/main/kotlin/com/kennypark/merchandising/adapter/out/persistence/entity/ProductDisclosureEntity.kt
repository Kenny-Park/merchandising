package com.kennypark.merchandising.adapter.out.persistence.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.Comment

// 상품고시정보 마스터
@Entity
@Table(name="PRODUCT_DISCLOSURE")
class ProductDisclosureEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @NotNull
    val productDisclosureKey:Long? = null,
) {
    @Comment("상품고시정보 제목")
    var productDisclosureTitle:String? = null

    @OneToOne(mappedBy = "productDisclosure")
    //@Comment("프로덕트 상세 정보")
    lateinit var productDetail: ProductDetailEntity


    @OneToMany(mappedBy = "productDisclosure", cascade = [CascadeType.ALL], targetEntity = ProductDisclosureDetailEntity::class)
    lateinit var productDisclosureDetails: List<ProductDisclosureDetailEntity>

}
