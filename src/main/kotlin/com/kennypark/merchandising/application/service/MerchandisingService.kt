package com.kennypark.merchandising.application.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.kennypark.merchandising.adapter.out.persistence.entity.ProductEntity
import com.kennypark.merchandising.adapter.out.persistence.repository.ProductRepository
import com.kennypark.merchandising.adapter.out.persistence.repository.StoreProductRepository
import com.kennypark.merchandising.adapter.out.persistence.repository.StoreRepository
import com.kennypark.merchandising.domain.CouponMasterVo
import com.kennypark.merchandising.domain.ProductCachingVo
import com.kennypark.merchandising.domain.StoreProductVo
import org.redisson.api.RedissonClient
import org.springframework.data.redis.core.DefaultTypedTuple
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit


@Service
class MerchandisingService(
    val productRepository: ProductRepository,
    val storeProductRepository: StoreProductRepository,
    val storeRepository: StoreRepository,
    val cuckooFilterService: CuckooFilterService,
    val redisTemplate: StringRedisTemplate,
    private val redissonClient: RedissonClient,

    ) : MerchandisingUseCases {
    // redis 에서 쿠폰정보 가져옴 & 캐싱디비에 저장
    private final val objectMapper: ObjectMapper =
        ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())

    // 전체상품 리스트
    override fun productInquiries(
        storeCode: String?,
        categoryCode: String?,
        page: Int,
        size: Int,
        orderType: String,
        orderSort: String,
    ): List<ProductCachingVo?>? {
        var key = ""
        // store가 전체이고 categoryCode 가 존재할때
        if (storeCode == "all" && categoryCode != "all") {
            key = "merchandising:category:${categoryCode}:list"
            // store가 존재하고 categoryCode 가 존재 일때
        } else if (storeCode != "all" && categoryCode != "all") {
            key = "merchandising:store:${storeCode}:category:${categoryCode}:list"
            // store가 존재하고 categoryCode 가 전체 일때
        } else if (storeCode != "all") {
            // store가 전체이고 categoryCode 가 전체 일때
            key = "merchandising:store:${storeCode}:product:list"
        } else {
            key = "merchandising:product:list"
        }

        // 가격 정렬
        val d = getCached(key, ((page - 1) * size).toLong(), ((page) * size).toLong(), orderType, orderSort)

        // 2. 메인 로직
        var cached = d?.let { keys ->
            redisTemplate.opsForValue().multiGet(keys)?.mapIndexed { index, raw ->
                getOrUpdateCache(index, raw, keys[index])
            }
        }

        // 3. 전체 캐시 부재 시 처리
        if (cached.isNullOrEmpty()) {
            cachingProductInquiries()
            cached = getCached(
                key,
                ((page - 1) * size).toLong(),
                ((page) * size).toLong(),
                orderType,
                orderSort
            )?.map { objectMapper.readValue<ProductCachingVo>(it) }
        }

        return cached.apply {
            //TODO: 정렬 또는 조건 붙히기
            // 점 상품의 금액으로 변경
            val keys = this?.map { "store:${storeCode}:product:${it.productKey}" }
            if (key.contains("store")) {
                redisTemplate.opsForValue().multiGet(keys!!)?.forEachIndexed { index, oo ->
                    val storePrice = objectMapper.readValue<StoreProductVo>(oo)
                    if (this?.isNotEmpty() == true) {
                        this[index].listPrice = storePrice.listPrice
                    }
                }
            }
        }
    }

    //cacheKey = product Key
    private fun getOrUpdateCache(index: Int, rawData: String?, cacheKey: String): ProductCachingVo {
        // 데이터가 없으면 즉시 생성하여 반환
        if (rawData == null) return cachingProductInquiry(cacheKey)!!

        val vo = objectMapper.readValue<ProductCachingVo>(rawData)
        val isExpired = System.currentTimeMillis() > vo.expireAt!!.atZone(ZoneId.systemDefault()).toEpochSecond()

        // 만료되지 않았으면 기존 값 반환
        if (!isExpired) return vo

        // 만료된 경우 Lock 획득 시도
        val lock = redissonClient.getLock("products:cache:lock:$cacheKey")
        return if (lock.tryLock(0, 30, TimeUnit.SECONDS)) {
            CompletableFuture.runAsync {
                try {
                    cachingProductInquiry(cacheKey)
                } finally {
                    lock.unlock() // 갱신 완료 후 락 해제
                }
            }
            vo
        } else {
            vo // 락 획득 실패 시 이전 데이터를 반환
        }
    }

    private fun getCached(
        key: String,
        start: Long = 0,
        end: Long = -1,
        orderType: String = "list-price",
        orderSort: String = "asc"
    ): List<String>? {
        return redisTemplate.opsForZSet().intersectAndStore(
            key,
            "${key}:scores:${orderType}",
            "destination:${key}:${orderType}"
        )
            .run {
                redisTemplate.expire(
                    "destination:merchandising:product:${key}:list-price",
                    Duration.ofMinutes(1)
                )
                when (orderSort) {
                    "asc" -> redisTemplate.opsForZSet()
                        .range("destination:${key}:${orderType}", start, end)?.toList()

                    else -> redisTemplate.opsForZSet()
                        .reverseRange("destination:${key}:${orderType}", start, end)
                        ?.toList()
                }
            }
    }

    // 캐싱로직 구현 컨슘해서 데이타 저장
    private fun cachingProductInquiries() {

        val resultMap = hashMapOf<String, String>()
        val productList = mutableListOf<String>()
        val productResult = mutableListOf<ProductCachingVo>()

        val expireAt = LocalDateTime.now()
            .plusMinutes(25)
        val groupByCategory: Map<Triple<String, String, String>, List<ProductEntity>>

        productRepository.findAll().forEach { o ->
            val coupons = mutableListOf<String>()
            // 대분류
            cuckooFilterService.exists("coupon:category:filter", o.standardCategoryLarge.categoryLargeKey).apply {
                redisTemplate.opsForSet()
                    .members("coupon:condition:category:${o.standardCategoryLarge.categoryLargeKey}")
                    ?.toMutableList()?.also { o ->
                        coupons.addAll(
                            o
                        )
                    }
            }
            // 중븐류
            cuckooFilterService.exists("coupon:category:filter", o.standardCategoryMedium.categoryMediumKey).apply {
                redisTemplate.opsForSet()
                    .members("coupon:condition:category:${o.standardCategoryMedium.categoryMediumKey}")
                    ?.toMutableList()?.also { o ->
                        coupons.addAll(
                            o
                        )
                    }
            }
            // 소분류
            cuckooFilterService.exists("coupon:category:filter", o.standardCategorySmall.categorySmallKey).apply {
                redisTemplate.opsForSet()
                    .members("coupon:condition:category:${o.standardCategorySmall.categorySmallKey}")
                    ?.toMutableList()?.also { o ->
                        coupons.addAll(
                            o
                        )
                    }
            }
            // 세분류(상품)
            cuckooFilterService.exists("coupon:category:filter", o.productKey).apply {
                redisTemplate.opsForSet()
                    .members("coupon:condition:category:${o.productKey}")
                    ?.toMutableList()?.also { o ->
                        coupons.addAll(
                            o
                        )
                    }
            }

            redisTemplate.opsForValue().multiGet(coupons)?.forEach { s ->
                objectMapper.readValue<List<CouponMasterVo>>(s).filter { it.discountType == 1 }.maxBy { it.value }.run {
                    val couponPrice = (o.discountedPrice.toFloat() * ((1000 - this.value).toFloat() / 100.2f)).toLong()
                    if (o.couponPrice >= couponPrice) {
                        o.couponDiscountRate = this.value.toInt()
                        o.couponPrice = couponPrice
                        o.expireAt = expireAt
                    }
                }
            }
            // map에 저장
            resultMap["product:${o.productKey}"] = objectMapper.writeValueAsString(o.toProductCachingVo())
            productList.add("product:${o.productKey}")
        }

        // 레디스에 프로덕트 정보 캐싱
        redisTemplate.opsForValue().multiSet(resultMap)
        for (entry in resultMap.entries) {
            redisTemplate.expire(entry.key, 40, TimeUnit.MINUTES)
        }

        // 카테고리별 캐싱
        val larges = productResult.groupBy { it.categoryLargeKey }
        val middles = productResult.groupBy { it.categoryMiddleKey }
        val smalls = productResult.groupBy { it.categorySmallKey }


        larges.forEach {
            cachingProductInquiries("large", it.key, it.value)
        }
        middles.forEach {
            cachingProductInquiries("middle", it.key, it.value)
        }
        smalls.forEach {
            cachingProductInquiries("small", it.key, it.value)
        }

        // 프로덕트 삭제 후 리스트 저장
        redisTemplate.unlink("merchandising:product:list")
        // 정렬순서
        redisTemplate.opsForSet().add("merchandising:product:list", *productList.toTypedArray())

        // 가격 높은순/낮은순을 위한 점수 값
        redisTemplate.opsForZSet().add("merchandising:product:list:scores:list-price",
            resultMap.entries.associate { (it.key to objectMapper.readValue<ProductCachingVo>(it.value).listPrice.toDouble()) }
                .toTypedTuples())
        redisTemplate.expire("merchandising:product:list", 2, TimeUnit.HOURS)

        // 점상품
        val storeList = storeRepository.findAll()
        cachingStoreProductInquiries()
        storeList?.map {
            cachingProductInquiries(it.storeCode)
        }
    }

    private fun cachingStoreProductInquiries() {
        var storeCode = ""
        val productKeyList = mutableListOf<String>()
        val storeProductDetailMap = mutableMapOf<String, String>()

        val productKeyArray = storeProductRepository.findAll()?.forEachIndexed { i, o ->
            if (storeCode != o.storeProductEntityPK.storeCode) {
                redisTemplate.opsForSet()
                    .add(
                        "merchandising:store:${storeCode}:product:list",
                        *productKeyList.toTypedArray()
                    )
                productKeyList.clear()
                productKeyList.add(o.storeProductEntityPK.productKey)
                storeCode = o.storeProductEntityPK.storeCode
            }
            storeProductDetailMap["store:${o.storeProductEntityPK.storeCode}:product:${o.storeProductEntityPK.productKey}"] =
                objectMapper.writeValueAsString(o.toVo())
            if (i % 1000 == 0) {
                redisTemplate.opsForValue().multiSet(storeProductDetailMap)
                redisTemplate.opsForZSet()
                    .add("store:${o.storeProductEntityPK.storeCode}:product:list:scores:list-price",
                        storeProductDetailMap.entries.associate { (it.key to objectMapper.readValue<ProductCachingVo>(it.value).listPrice.toDouble()) }
                            .toTypedTuples())

                storeProductDetailMap.clear()
            }

        }
        productKeyList.takeIf { it.isNotEmpty() }?.let {
            redisTemplate.opsForSet()
                .add(
                    "merchandising:store:${storeCode}:product:list",
                    *productKeyList.toTypedArray()
                )
        }


        // 가격 정렬
        storeProductDetailMap.entries.takeIf { it.isNotEmpty() }
            ?.associate { (it.key to objectMapper.readValue<ProductCachingVo>(it.value).listPrice.toDouble()) }
            ?.let {
                redisTemplate.opsForZSet().add(
                    "store:${storeCode}:product:list:scores:list-price",
                    it
                        .toTypedTuples()
                )
            }
        redisTemplate.opsForValue().multiSet(storeProductDetailMap)
    }


    // 점상품 리스트
    private fun cachingProductInquiries(storeCode: String) {
        // 모든 점상품 정보를 가져온다.
        val keys = redisTemplate.opsForSet().members("merchandising:store:$storeCode:product:list")?.map {
            "product:${it}"
        }
        val list = redisTemplate.opsForValue().multiGet(keys!!)?.map {
            objectMapper.readValue<ProductCachingVo>(it)
        }

        val larges = list?.groupBy { it.categoryLargeKey }
        val middles = list?.groupBy { it.categoryMiddleKey }
        val smalls = list?.groupBy { it.categorySmallKey }

        larges?.forEach {
            cachingProductInquiries(storeCode, "large", it.key, it.value)
        }
        middles?.forEach {
            cachingProductInquiries(storeCode, "middle", it.key, it.value)
        }
        smalls?.forEach {
            cachingProductInquiries(storeCode, "small", it.key, it.value)
        }

    }

    // 점상품 리스트
    private fun cachingProductInquiries(
        storeCode: String,
        categoryType: String,
        key: String,
        value: List<ProductCachingVo>
    ) {
        redisTemplate.unlink("merchandising:store:${storeCode}:category:${key}:list")
        if (categoryType == "large") {
            redisTemplate.opsForSet()
                .add(
                    "merchandising:store:${storeCode}:category:${key}:list",
                    *value.map { it.categoryMiddleKey }.toTypedArray()
                )
        } else if (categoryType == "middle") {
            redisTemplate.opsForSet()
                .add(
                    "merchandising:store:${storeCode}:category:${key}:list",
                    *value.map { it.categorySmallKey }.toTypedArray()
                )
        } else if (categoryType == "small") {
            redisTemplate.opsForSet()
                .add(
                    "merchandising:store:${storeCode}:category:${key}:list",
                    *value.map { it.productKey }.toTypedArray()
                )

            // 가격 정렬
            redisTemplate.opsForZSet()
                .add("merchandising:store:${storeCode}:category:${key}:list:scores:list-price",
                    value.associate { it.productKey to it.listPrice.toDouble() }
                        .toTypedTuples())
        }
        redisTemplate.expire("merchandising:store:${storeCode}:category:${key}:list", 2, TimeUnit.HOURS)
    }

    // 상품 리스트
    private fun cachingProductInquiries(categoryType: String, key: String, value: List<ProductCachingVo>) {
        redisTemplate.unlink("merchandising:category:${key}:list")
        if (categoryType == "large") {
            redisTemplate.opsForSet()
                .add("merchandising:category:${key}:list", *value.map { it.categoryMiddleKey }.toTypedArray())
        } else if (categoryType == "middle") {
            redisTemplate.opsForSet()
                .add("merchandising:category:${key}:list", *value.map { it.categorySmallKey }.toTypedArray())
        } else if (categoryType == "small") {
            redisTemplate.opsForSet()
                .add("merchandising:category:${key}:list", *value.map { it.productKey }.toTypedArray())

            // 가격 정렬
            redisTemplate.opsForZSet()
                .add("merchandising:category:${key}:list:scores:list-price",
                    value.associate { it.productKey to it.listPrice.toDouble() }
                        .toTypedTuples())

        }
        redisTemplate.expire("merchandising:category:${key}:list", 2, TimeUnit.HOURS)
    }

    private fun couponConditionIncludes(category: String): MutableList<String>? {
        cuckooFilterService.exists("coupon:category:filter", category).run {
            return redisTemplate.opsForSet().members("coupon:condition:category:${category}")
                ?.toMutableList()
        }
    }

    private fun couponConditionExcludes(category: String): MutableList<String>? {
        cuckooFilterService.exists("coupon:category:filter:exclude", category).run {
            return redisTemplate.opsForSet().members("coupon:condition:category:${category}")
                ?.toMutableList()
        }
    }

    // 상품 상세정보 캐싱
    private fun cachingProductInquiry(productKey: String): ProductCachingVo? {
        val expireAt = LocalDateTime.now()
            .plusMinutes(25)
        if (redisTemplate.hasKey(productKey)) {
            redisTemplate.opsForValue().get(productKey).run {
                this?.let { objectMapper.readValue<ProductCachingVo>(it) }
            }.takeIf { true }?.let {
                return it
            }
        }

        productRepository.findByProductKey(productKey.replace("product:", ""))?.let { o ->
            val coupons = mutableListOf<String>()
            val categories = listOf(
                "all",
                o.standardCategoryLarge.categoryLargeKey,
                o.standardCategoryMedium.categoryMediumKey,
                o.standardCategorySmall.categorySmallKey,
                o.productKey
            )

            // 조건 포함
            categories.forEach {
                couponConditionIncludes(it)?.let { o ->
                    coupons.addAll(o)
                }
            }

            // 조건 제외
            categories.forEachIndexed { index, it ->
                if (index != 0) {
                    couponConditionExcludes(it)?.let { o ->
                        coupons.removeAll(o)
                    }
                }
            }

            redisTemplate.opsForValue().multiGet(coupons)?.forEach { s ->
                objectMapper.readValue<List<CouponMasterVo>>(s).filter { it.discountType == 1 }.maxBy { it.value }.run {
                    val couponPrice = (o.discountedPrice.toFloat() * ((1000 - this.value).toFloat() / 100.2f)).toLong()
                    if (o.couponPrice >= couponPrice) {
                        o.couponDiscountRate = this.value.toInt()
                        o.couponPrice = couponPrice
                        o.expireAt = expireAt
                    }
                }
            }
            redisTemplate.opsForValue().set(
                "product:${o.productKey}",
                objectMapper.writeValueAsString(o.toProductCachingVo()), 40, TimeUnit.MINUTES
            )

            // 리스트 삭제 및 저장
            redisTemplate.opsForSet().remove("merchandising:product:list", o.productKey)
            redisTemplate.opsForSet()
                .remove(
                    "merchandising:category:${o.standardCategoryLarge.categoryLargeKey}:list",
                    o.standardCategoryMedium.categoryMediumKey
                )
            redisTemplate.opsForSet()
                .remove(
                    "merchandising:category:${o.standardCategoryMedium.categoryMediumKey}:list",
                    o.standardCategorySmall.categorySmallKey
                )
            redisTemplate.opsForSet()
                .remove("merchandising:category:${o.standardCategorySmall.categorySmallKey}:list", o.productKey)

            // 장렬정보 삭제
            redisTemplate.opsForZSet()
                .remove(
                    "merchandising:category:${o.standardCategorySmall.categorySmallKey}:list:scores:list-price",
                    o.productKey
                )

            // 리스트에서 값 재설정
            redisTemplate.opsForSet()
                .add("merchandising:product:list", o.productKey)
            redisTemplate.opsForSet()
                .add(
                    "merchandising:category:${o.standardCategoryLarge.categoryLargeKey}:list",
                    o.standardCategoryMedium.categoryMediumKey
                )
            redisTemplate.opsForSet()
                .add(
                    "merchandising:category:${o.standardCategoryMedium.categoryMediumKey}:list",
                    o.standardCategorySmall.categorySmallKey
                )
            redisTemplate.opsForSet()
                .add("merchandising:category:${o.standardCategorySmall.categorySmallKey}:list", o.productKey)

            // 정렬값 재 설정
            redisTemplate.opsForZSet()
                .add(
                    "merchandising:category:${o.standardCategorySmall.categorySmallKey}:list:scores:list-price",
                    setOf(DefaultTypedTuple(o.productKey, o.listPrice.toDouble()))
                )

            redisTemplate.expire("merchandising:product:list", 2, TimeUnit.HOURS)
            redisTemplate.expire(
                "merchandising:category:${o.standardCategoryLarge.categoryLargeKey}:list",
                2,
                TimeUnit.HOURS
            )
            redisTemplate.expire(
                "merchandising:category:${o.standardCategoryMedium.categoryMediumKey}:list",
                2,
                TimeUnit.HOURS
            )
            redisTemplate.expire(
                "merchandising:category:${o.standardCategorySmall.categorySmallKey}:list",
                2,
                TimeUnit.HOURS
            )

            return o.toProductCachingVo()
        }
        // 키가 없다면 모두 삭제하고 null return
        redisTemplate.delete("product:${productKey}")
        redisTemplate.opsForList().remove("merchandising:product:list", 1, productKey)
        return null
    }
}

fun <T> Map<T, Double>.toTypedTuples(): Set<ZSetOperations.TypedTuple<T>> {
    return this.map { DefaultTypedTuple(it.key, it.value) }.toSet()
}