package com.kennypark.merchandising.domain

import com.kennypark.merchandising.domain.ErrorVo


class CustomException (
    val errorVo:ErrorVo,
): RuntimeException() {

}
