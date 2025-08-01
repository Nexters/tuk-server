package nexters.tuk.contract

import org.springframework.http.HttpStatus

enum class ErrorType(val status: HttpStatus, val code: String, val message: String) {
    // standard
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYSTEM_ERROR", "일시적인 오류가 발생했습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "권한 인증에 실패했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "잘못된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "존재하지 않는 요청입니다."),

    // push
    FAIL_TO_SEND_PUSH(HttpStatus.INTERNAL_SERVER_ERROR, "FAIL_TO_SEND_PUSH", "푸시 발송에 실패하였습니다."),
}
