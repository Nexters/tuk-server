package nexters.tuk.ui.resolver

import jakarta.servlet.http.HttpServletRequest
import nexters.tuk.application.auth.JwtProvider
import nexters.tuk.contract.BaseException
import nexters.tuk.contract.ErrorType
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class AccessTokenResolver(
    private val jwtTokenProvider: JwtProvider
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(Authenticated::class.java)
                && parameter.parameterName == "memberId"
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any {
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
        val token = extractToken(request) ?: throw BaseException(ErrorType.UNAUTHORIZED, "AccessToken이 존재하지 않습니다.")
        return jwtTokenProvider.validateAccessTokenAndGetMemberId(token)
    }

    private fun extractToken(request: HttpServletRequest?): String? {
        val header = request?.getHeader("Authorization") ?: return null
        if (!header.startsWith("Bearer ")) return null
        return header.removePrefix("Bearer ").trim()
    }
}