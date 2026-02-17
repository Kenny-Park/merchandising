package com.kennypark.merchandising.adapter.out.persistence.entity

import com.kennypark.merchandising.domain.PublicTypeEnum
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.Comment
import org.hibernate.annotations.UpdateTimestamp
import org.jetbrains.annotations.Nullable
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime

// 상품고시정보 마스터
@Entity
@Comment("상품문의")
@Table(name="QNA")
class QnaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @NotNull
    val qnaKey:Long? = null,
) {
    @Comment("문의")
    var question:String? = null

    @Comment("답변")
    var answer:String? = null

    @Comment("고객키")
    var memberKey:String? = null

    @Comment("관리자키")
    var adminKey:String? = null

    @Comment("공개/비공개 여부")
    @Enumerated(EnumType.STRING)
    var publicType: PublicTypeEnum = PublicTypeEnum.Public

    @CreatedDate
    @Comment("등록일")
    lateinit var registeredDate: LocalDateTime

    @UpdateTimestamp
    @Nullable
    @Comment("수정일")
    var updatedDate: LocalDateTime? = null

    @Nullable
    @Comment("답변일")
    var answeredDate: LocalDateTime? = null

    @Comment("상품키")
    var productKey: String? = null
}
