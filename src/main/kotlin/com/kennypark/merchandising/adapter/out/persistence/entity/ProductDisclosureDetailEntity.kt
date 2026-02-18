package com.kennypark.merchandising.adapter.out.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.Comment

@Entity
@Table(name="PRODUCT_DISCLOSURE_DETAIL")
class ProductDisclosureDetailEntity(
    @EmbeddedId
    val productDisclosureEntityPK:ProductDisclosureEntityPK,
) {
    @Comment("상품고시정보 상세 제목")
    var productDisclosureSubject:String? = null

    @Comment("상품고시정보 내용")
    var productDisclosureDescription:String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productDisclosureKey")
    @JoinColumn(name = "productDisclosureKey")
    lateinit var productDisclosure:ProductDisclosureEntity
}
