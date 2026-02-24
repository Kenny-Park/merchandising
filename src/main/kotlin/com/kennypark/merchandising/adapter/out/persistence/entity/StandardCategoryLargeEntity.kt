package com.kennypark.merchandising.adapter.out.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.Comment

@Entity
@Table(name="STANDARD_CATEGORY_LARGE")
class StandardCategoryLargeEntity(
    @Id
    @Comment("대분류키")
    val categoryLargeKey:String,
) {
    @Comment("대분류명")
    var categoryLargeKeyName:String? = null

    @OneToOne(mappedBy = "standardCategoryLarge")
    lateinit var product: ProductEntity
}