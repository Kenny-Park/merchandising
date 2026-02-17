package com.kennypark.merchandising.adapter.out.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType

@Embeddable
data class ProductDisclosureEntityPK (
    @Column(name = "productDisclosureKey")
    val productDisclosureKey:Long,
    // 순번
    val productDisclosureOrder:Int
)