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
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
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

    ):MerchandisingUseCases {
    // redis 에서 쿠폰정보 가져옴 & 캐싱디비에 저장
    private final val objectMapper: ObjectMapper =
        ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())

    // 전체상품 리스트
    override fun productInquiries(storeCode: String, categoryCode:String): List<ProductCachingVo?>? {
        var key = "merchandising:product:list"
        // store가 전체이고 categoryCode 가 존재할때
        if (storeCode == "all" && categoryCode != "all") {
            key = "merchandising:category:${categoryCode}:list"
            // store가 존재하고 categoryCode 가 존재 일때
        } else if (storeCode != "all" && categoryCode != "all") {
            key = "merchandising:store:${storeCode}:category:${categoryCode}:list"
            // store가 존재하고 categoryCode 가 전체 일때
        } else if (storeCode != "all" ) {
            // store가 전체이고 categoryCode 가 전체 일때
            key = "merchandising:store:${storeCode}:product:list"
        } else {
            key = "merchandising:product:list"
        }

        val d = getCached(key)

        // 2. 메인 로직
        var cached = d?.let { keys ->
            redisTemplate.opsForValue().multiGet(keys)?.mapIndexed { index, raw ->
                getOrUpdateCache(index, raw, keys[index])
            }
        }

        // 3. 전체 캐시 부재 시 처리
        if (cached.isNullOrEmpty()) {
            cachingProductInquiries()
            cached = getCached(key)?.map { objectMapper.readValue<ProductCachingVo>(it) }
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

    private fun getOrUpdateCache(index: Int, rawData: String?, cacheKey: String): ProductCachingVo {
        // 데이터가 없으면 즉시 생성하여 반환
        if (rawData == null) return cachingProductInquiry(cacheKey)!!

        val vo = objectMapper.readValue<ProductCachingVo>(rawData)
        val isExpired = System.currentTimeMillis() > vo.expireAt!!.atZone(ZoneId.systemDefault()).toEpochSecond()

        // 만료되지 않았으면 기존 값 반환
        if (!isExpired) return vo

        // 만료된 경우 Lock 획득 시도
        val lock = redissonClient.getLock("products:cache:lock:$cacheKey") // 키별로 락을 거는 것이 효율적입니다
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
            vo // 락 획득 실패 시 이전 데이터(Stale data)라도 반환
        }
    }

    private fun getCached(key: String): List<String>? {
        return redisTemplate.opsForList().range("merchandising:product:list", 0, -1)
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
            cuckooFilterService.exists("coupon:category:filter", o.categoryLargeKey).run {
                redisTemplate.opsForSet()
                    .members("coupon:condition:category:${o.categoryLargeKey}")
                    ?.toMutableList()?.apply {
                        coupons.addAll(
                            this
                        )
                    }
            }
            // 중븐류
            cuckooFilterService.exists("coupon:category:filter", o.categoryMiddleKey).run {
                redisTemplate.opsForSet()
                    .members("coupon:condition:category:${o.categoryMiddleKey}")
                    ?.toMutableList()?.apply {
                        coupons.addAll(
                            this
                        )
                    }
            }
            // 소분류
            cuckooFilterService.exists("coupon:category:filter", o.categorySmallKey).run {
                redisTemplate.opsForSet()
                    .members("coupon:condition:category:${o.categorySmallKey}")
                    ?.toMutableList()?.apply {
                        coupons.addAll(
                            this
                        )
                    }
            }
            // 세분류(상품)
            cuckooFilterService.exists("coupon:category:filter", o.productKey).run {
                redisTemplate.opsForSet()
                    .members("coupon:condition:category:${o.productKey}")
                    ?.toMutableList()?.apply {
                        coupons.addAll(
                            this
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
            productList.add(o.productKey)
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
        redisTemplate.opsForList()
            .rightPushAll("merchandising:product:list", productList)
        redisTemplate.expire("merchandising:product:list", 2, TimeUnit.HOURS)

        // 점상품
        val storeList = storeRepository.findAll()
        cachingStoreProductInquiries()
        storeList?.map {
            cachingProductInquiries(it.storeCode)
        }
    }

    private fun cachingStoreProductInquiries() {
        // store 전체 상품코드 넣기
        storeProductRepository.findAll()?.forEach {
            redisTemplate.opsForList()
                .rightPush(
                    "merchandising:store:${it.storeProductEntityPK.storeCode}:product:list",
                    it.storeProductEntityPK.productKey
                )

            // 점 상품 정보 저장
            redisTemplate.opsForValue().set(
                "store:${it.storeProductEntityPK.storeCode}:product:${it.storeProductEntityPK.productKey}",
                objectMapper.writeValueAsString(it.toVo())
            )
        }
    }

    // 점상품 리스트
    private fun cachingProductInquiries(storeCode: String) {
        // 현재 정보 삭제
        val keys = redisTemplate.opsForList().range("merchandising:store:$storeCode:product:list", 0, -1)?.map {
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
            redisTemplate.opsForList()
                .rightPushAll(
                    "merchandising:store:${storeCode}:category:${key}:list",
                    value.map { it.categoryMiddleKey })
        } else if (categoryType == "middle") {
            redisTemplate.opsForList()
                .rightPushAll(
                    "merchandising:store:${storeCode}:category:${key}:list",
                    value.map { it.categorySmallKey })
        } else if (categoryType == "small") {
            redisTemplate.opsForList()
                .rightPushAll("merchandising:store:${storeCode}:category:${key}:list", value.map { it.productKey })
        }
        redisTemplate.expire("merchandising:store:${storeCode}:category:${key}:list", 2, TimeUnit.HOURS)
    }

    // 상품 리스트
    private fun cachingProductInquiries(categoryType: String, key: String, value: List<ProductCachingVo>) {
        redisTemplate.unlink("merchandising:category:${key}:list")
        if (categoryType == "large") {
            redisTemplate.opsForList()
                .rightPushAll("merchandising:category:${key}:list", value.map { it.categoryMiddleKey })
        } else if (categoryType == "middle") {
            redisTemplate.opsForList()
                .rightPushAll("merchandising:category:${key}:list", value.map { it.categorySmallKey })
        } else if (categoryType == "small") {
            redisTemplate.opsForList()
                .rightPushAll("merchandising:category:${key}:list", value.map { it.productKey })
        }
        redisTemplate.expire("merchandising:category:${key}:list", 2, TimeUnit.HOURS)
    }
    private fun couponConditionIncludes(category:String):MutableList<String>? {
        cuckooFilterService.exists("coupon:category:filter", category).run {
            return redisTemplate.opsForSet().members("coupon:condition:category:${category}")
                ?.toMutableList()
        }
    }
    private fun couponConditionExcludes(category:String):MutableList<String>? {
        cuckooFilterService.exists("coupon:category:filter:exclude", category).run {
            return redisTemplate.opsForSet().members("coupon:condition:category:${category}")
                ?.toMutableList()
        }
    }

    // 상품 상세정보 캐싱
    private fun cachingProductInquiry(productKey: String): ProductCachingVo? {
        val expireAt = LocalDateTime.now()
            .plusMinutes(25)
        productRepository.findByProductKey(productKey)?.let { o ->
            val coupons = mutableListOf<String>()
            val categories = listOf("all", o.categoryLargeKey, o.categoryMiddleKey, o.categorySmallKey, o.productKey)

            // 조건 포함
            categories.forEach {
                couponConditionIncludes(it)?.let{o->
                    coupons.addAll(o)
                }
            }

            // 조건 제외
            categories.forEachIndexed { index, it ->
                if (index != 0) {
                    couponConditionExcludes(it)?.let{o->
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
            redisTemplate.opsForList().remove("merchandising:product:list", 1, o.productKey)
            redisTemplate.opsForList().remove("merchandising:category:${o.categoryLargeKey}:list", 1, o.categoryMiddleKey)
            redisTemplate.opsForList().remove("merchandising:category:${o.categoryMiddleKey}:list", 1, o.categorySmallKey)
            redisTemplate.opsForList().remove("merchandising:category:${o.categorySmallKey}:list", 1, o.productKey)

            redisTemplate.opsForList()
                .rightPush("merchandising:product:list", o.productKey)
            redisTemplate.opsForList()
                .rightPush("merchandising:category:${o.categoryLargeKey}:list", o.categoryMiddleKey)
            redisTemplate.opsForList()
                .rightPush("merchandising:category:${o.categoryMiddleKey}:list", o.categorySmallKey)
      redisTemplate.opsForList()
                .rightPush("merchandising:category:${o.categorySmallKey}:list", o.productKey)

            redisTemplate.expire("merchandising:product:list", 2, TimeUnit.HOURS)
            redisTemplate.expire("merchandising:category:${o.categoryLargeKey}:list", 2, TimeUnit.HOURS)
            redisTemplate.expire("merchandising:category:${o.categoryMiddleKey}:list", 2, TimeUnit.HOURS)
            redisTemplate.expire("merchandising:category:${o.categorySmallKey}:list", 2, TimeUnit.HOURS)

            return o.toProductCachingVo()
        }
        // 키가 없다면 모두 삭제하고 null return
        redisTemplate.delete("product:${productKey}")
        redisTemplate.opsForList().remove("merchandising:product:list", 1, productKey)
        return null
    }
}