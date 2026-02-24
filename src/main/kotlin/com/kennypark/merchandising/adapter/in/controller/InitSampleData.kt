package com.kennypark.merchandising.adapter.`in`.controller

import com.kennypark.merchandising.adapter.out.persistence.entity.ProductEntity
import com.kennypark.merchandising.adapter.out.persistence.entity.StandardCategoryLargeEntity
import com.kennypark.merchandising.adapter.out.persistence.entity.StandardCategoryMediumEntity
import com.kennypark.merchandising.adapter.out.persistence.entity.StandardCategorySmallEntity
import com.kennypark.merchandising.adapter.out.persistence.repository.ProductRepository
import com.kennypark.merchandising.adapter.out.persistence.repository.StandardCategoryLargeJpaRepository
import com.kennypark.merchandising.adapter.out.persistence.repository.StandardCategoryMediumJpaRepository
import com.kennypark.merchandising.adapter.out.persistence.repository.StandardCategorySmallJpaRepository
import org.springframework.boot.context.event.ApplicationReadyEvent

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.random.Random

@Component
class InitSampleData(
    val productRepository: ProductRepository,
    val standardCategoryLargeJpaRepository: StandardCategoryLargeJpaRepository,
    val standardCategoryMediumJpaRepository: StandardCategoryMediumJpaRepository,
    val standardCategorySmallJpaRepository: StandardCategorySmallJpaRepository

) {
    data class CategoryMap(
        val large: String,
        val middle: String,
        val small: String,
        val smallName: String
    )

    val categoryList = listOf(
        CategoryMap("L001", "M001", "S001", "에어프라이어"),
        CategoryMap("L001", "M001", "S002", "믹서기"),
        CategoryMap("L001", "M002", "S003", "청소기"),
        CategoryMap("L001", "M002", "S004", "공기청정기"),
        CategoryMap("L002", "M003", "S005", "셔츠"),
        CategoryMap("L002", "M003", "S006", "바지"),
        CategoryMap("L002", "M004", "S007", "원피스"),
        CategoryMap("L002", "M004", "S008", "블라우스"),
        CategoryMap("L003", "M005", "S009", "사과"),
        CategoryMap("L003", "M005", "S010", "한우"),
        CategoryMap("L003", "M006", "S011", "라면"),
        CategoryMap("L003", "M006", "S012", "과자"),
        CategoryMap("L004", "M007", "S013", "소파"),
        CategoryMap("L004", "M007", "S014", "침대"),
        CategoryMap("L004", "M008", "S015", "데스크램프"),
        CategoryMap("L004", "M008", "S016", "무드등"),
        CategoryMap("L005", "M009", "S017", "텐트"),
        CategoryMap("L005", "M009", "S018", "침낭"),
        CategoryMap("L005", "M010", "S019", "골프클럽"),
        CategoryMap("L005", "M010", "S020", "골프공")
    )

    @EventListener(ApplicationReadyEvent::class)
    @Transactional
    fun generateProductSamples() {
        val products = mutableListOf<ProductEntity>()
        val random = Random(System.currentTimeMillis())

        for (i in 1..1000) {
            // 1. 랜덤 카테고리 선택
            val cat = categoryList[random.nextInt(categoryList.size)]

            // 2. 가격 계산 (정상가 10,000 ~ 1,000,000원)
            val listPrice = (random.nextLong(10, 1001)) * 1000L
            val discountRate = 10
            val discountedPrice = (listPrice * (100 - discountRate) / 100)

            // 3. Entity 생성 및 필드 주입
            val entity = ProductEntity(
                productKey = "PROD-${i.toString().padStart(5, '0')}",
            ).apply {
                this.listPrice = listPrice
                this.discountRate = discountRate
                this.discountedPrice = discountedPrice
                this.couponDiscountRate = 5
                this.couponPrice = (discountedPrice * 95 / 100)

                this.productKoreanName = "${cat.smallName} 샘플 상품 $i"
                this.productEnglishName = "Sample Product ${cat.smallName} $i"

                this.displayStartDate = LocalDateTime.now()
                this.displayEndDate = LocalDateTime.now().plusMonths(1)
                this.expireAt = LocalDateTime.now().plusYears(1)
                this.standardCategoryLarge = standardCategoryLargeJpaRepository.findById(cat.large).orElseThrow()
                this.standardCategoryMedium = standardCategoryMediumJpaRepository.findById(cat.middle).orElseThrow()
                this.standardCategorySmall = standardCategorySmallJpaRepository.findById(cat.small).orElseThrow()
                // 1:1 관계인 상세 데이터 가상 할당 (필요 시)
                /*this.productDetail = ProductDetailEntity().apply {
                    // detail 필드들 세팅...
                }*/
            }
            products.add(entity)
        }
        productRepository.saveAll(products)
    }
}