package com.kennypark.merchandising.application.service


import com.kennypark.merchandising.domain.CustomException
import com.kennypark.merchandising.domain.ErrorVo
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class JwtAccessDeniedHandler : AccessDeniedHandler {

    override fun handle(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        accessDeniedException: org.springframework.security.access.AccessDeniedException?
    ) {
        val e = request?.getAttribute("exception") as Exception?
            ?: throw CustomException(ErrorVo(code = "400", message = "response 객체 없음"))

        response?.also {
            it.status = 401
            it.contentType = "application/json;charset=UTF-8"
            it.writer.write("""{"code":"${HttpServletResponse.SC_UNAUTHORIZED}", "message":"${e.message ?: ""}"}""")
        } ?: throw CustomException(ErrorVo(code = "400", message = "response 객체 없음"))
    }
}