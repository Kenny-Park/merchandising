package com.kennypark.merchandising.adapter.out.persistence.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.Comment

@Entity
@Table(name="DELIVERY_DESCRIPTION")
class DeliveryDescriptionEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val deliveryDescriptionKey:Long? = null
) {
    @NotNull
    @Comment("배송 정보 설명")
    lateinit var deliveryDescription:String

    @OneToOne(mappedBy = "deliveryDescription")
    //@Comment("프로덕트 상세 정보")
    lateinit var productDetail: ProductDetailEntity

}