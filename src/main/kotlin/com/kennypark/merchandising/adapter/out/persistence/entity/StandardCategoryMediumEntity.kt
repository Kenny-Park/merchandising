package com.kennypark.merchandising.adapter.out.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Comment

@Entity
@Table(name="STANDARD_CATEGORY_MEDIUM")
class StandardCategoryMediumEntity(
    @Id
    @Comment("중분류키")
    val categoryMediumKey: String,
    @Comment("대분류키")
    val categoryLargeKey: String,
) {
    @Comment("중분류명")
    var categoryMediumKeyName: String? = null

    @OneToOne(mappedBy = "standardCategoryMedium")
    lateinit var product: ProductEntity
}