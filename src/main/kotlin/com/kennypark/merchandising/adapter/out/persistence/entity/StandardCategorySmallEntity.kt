package com.kennypark.merchandising.adapter.out.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Comment

@Entity
@Table(name="STANDARD_CATEGORY_SMALL")
class StandardCategorySmallEntity (
    @Id
    @Comment("소분류키")
    val categorySmallKey: String,
    @Comment("대분류키")
    val categoryLargeKey: String,
    @Comment("중분류키")
    val categoryMediumKey: String,
) {
    @Comment("소분류명")
    var categorySmallKeyName: String? = null
}