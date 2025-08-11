package nexters.tuk.ui

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import nexters.tuk.contract.ApiResponse
import nexters.tuk.contract.BaseException
import nexters.tuk.contract.ErrorType
import nexters.tuk.application.alert.ErrorAlert
import nexters.tuk.application.alert.ErrorAlertSender
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.MissingRequestValueException
import org.springframework.web.server.ServerWebInputException
import org.springframework.web.servlet.resource.NoResourceFoundException
import jakarta.servlet.http.HttpServletRequest
import java.time.ZonedDateTime

@RestControllerAdvice
class ApiControllerAdvice(
    private val errorAlertSender: ErrorAlertSender
) {
    private val log = LoggerFactory.getLogger(ApiControllerAdvice::class.java)

    @ExceptionHandler
    fun handle(e: BaseException, request: HttpServletRequest): ResponseEntity<ApiResponse<*>> {
        log.warn("BaseException : {}", e.message, e)
        sendSlackAlert(request, e.errorType, e.message)
        return failureResponse(errorType = e.errorType, errorMessage = e.message)
    }

    @ExceptionHandler
    fun handle(e: IllegalArgumentException, request: HttpServletRequest): ResponseEntity<ApiResponse<*>> {
        log.warn("BaseException : {}", e.message, e)
        sendSlackAlert(request, ErrorType.BAD_REQUEST, e.message)
        return failureResponse(errorType = ErrorType.BAD_REQUEST, errorMessage = e.message)
    }

    @ExceptionHandler
    fun handle(e: MissingRequestValueException, request: HttpServletRequest): ResponseEntity<ApiResponse<*>> {
        val name = e.methodParameter?.parameter?.name
        val type = e.methodParameter?.parameter?.type?.simpleName
        val message = "필수 요청 파라미터 '$name' (타입: $type)가 누락되었습니다."
        sendSlackAlert(request, ErrorType.BAD_REQUEST, message)
        return failureResponse(errorType = ErrorType.BAD_REQUEST, errorMessage = message)
    }

    @ExceptionHandler
    fun handle(e: HttpMessageNotReadableException, request: HttpServletRequest): ResponseEntity<ApiResponse<*>> {
        val errorMessage = when (val rootCause = e.rootCause) {
            is InvalidFormatException -> {
                val fieldName = rootCause.path.joinToString(".") { it.fieldName ?: "?" }

                val valueIndicationMessage = when {
                    rootCause.targetType.isEnum -> {
                        val enumClass = rootCause.targetType
                        val enumValues = enumClass.enumConstants.joinToString(", ") { it.toString() }
                        "사용 가능한 값 : [$enumValues]"
                    }

                    else -> ""
                }

                val expectedType = rootCause.targetType.simpleName
                val value = rootCause.value

                "필드 '$fieldName'의 값 '$value'이(가) 예상 타입($expectedType)과 일치하지 않습니다. $valueIndicationMessage"
            }

            is MismatchedInputException -> {
                val fieldPath = rootCause.path.joinToString(".") { it.fieldName ?: "?" }
                "필수 필드 '$fieldPath'이(가) 누락되었습니다."
            }

            is JsonMappingException -> {
                val fieldPath = rootCause.path.joinToString(".") { it.fieldName ?: "?" }
                "필드 '$fieldPath'에서 JSON 매핑 오류가 발생했습니다: ${rootCause.originalMessage}"
            }

            else -> "요청 본문을 처리하는 중 오류가 발생했습니다. JSON 메세지 규격을 확인해주세요."
        }

        sendSlackAlert(request, ErrorType.BAD_REQUEST, errorMessage)
        return failureResponse(errorType = ErrorType.BAD_REQUEST, errorMessage = errorMessage)
    }

    @ExceptionHandler
    fun handleBadRequest(e: ServerWebInputException, request: HttpServletRequest): ResponseEntity<ApiResponse<*>> {
        val errorMessage = when (val rootCause = e.rootCause) {
            is InvalidFormatException -> {
                val fieldName = rootCause.path.joinToString(".") { it.fieldName ?: "?" }

                val valueIndicationMessage = when {
                    rootCause.targetType.isEnum -> {
                        val enumClass = rootCause.targetType
                        val enumValues = enumClass.enumConstants.joinToString(", ") { it.toString() }
                        "사용 가능한 값 : [$enumValues]"
                    }

                    else -> ""
                }

                val expectedType = rootCause.targetType.simpleName
                val value = rootCause.value

                "필드 '$fieldName'의 값 '$value'이(가) 예상 타입($expectedType)과 일치하지 않습니다. $valueIndicationMessage"
            }

            is MismatchedInputException -> {
                val fieldPath = rootCause.path.joinToString(".") { it.fieldName ?: "?" }
                "필수 필드 '$fieldPath'이(가) 누락되었습니다."
            }

            is JsonMappingException -> {
                val fieldPath = rootCause.path.joinToString(".") { it.fieldName ?: "?" }
                "필드 '$fieldPath'에서 JSON 매핑 오류가 발생했습니다: ${rootCause.originalMessage}"
            }

            is IllegalArgumentException -> rootCause.message

            else -> "요청 본문을 처리하는 중 오류가 발생했습니다. JSON 메세지 규격을 확인해주세요."
        }

        sendSlackAlert(request, ErrorType.BAD_REQUEST, errorMessage)
        return failureResponse(errorType = ErrorType.BAD_REQUEST, errorMessage = errorMessage)
    }

    @ExceptionHandler
    fun handleNotFound(e: NoResourceFoundException, request: HttpServletRequest): ResponseEntity<ApiResponse<*>> {
        val message = "리소스를 찾을 수 없습니다: ${request.requestURI}"
        sendSlackAlert(request, ErrorType.NOT_FOUND, message)
        return failureResponse(errorType = ErrorType.NOT_FOUND)
    }

    @ExceptionHandler
    fun handle(e: Throwable, request: HttpServletRequest): ResponseEntity<ApiResponse<*>> {
        log.error("Exception : {}", e.message, e)
        val message = e.message ?: "알 수 없는 서버 오류가 발생했습니다"
        sendSlackAlert(request, ErrorType.INTERNAL_ERROR, message)
        return failureResponse(errorType = ErrorType.INTERNAL_ERROR)
    }

    private fun sendSlackAlert(request: HttpServletRequest, errorType: ErrorType, errorMessage: String? = null) {
        errorAlertSender.sendAlert(ErrorAlert(errorType.status.value(), request.method, request.requestURI, ZonedDateTime.now(), errorMessage ?: errorType.message))
    }

    private fun failureResponse(errorType: ErrorType, errorMessage: String? = null): ResponseEntity<ApiResponse<*>> =
        ResponseEntity(
            ApiResponse.fail(errorType = errorType, errorMessage = errorMessage ?: errorType.message),
            errorType.status,
        )
}
