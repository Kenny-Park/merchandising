package com.kennypark.merchandising.domain

import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

class ErrorVo(
    val code :String,
    val message:String,
    val data: JvmType.Object? = null
) {
}