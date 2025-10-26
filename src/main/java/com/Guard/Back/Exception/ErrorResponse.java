package com.Guard.Back.Exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

/*전역 예외 처리 시 클라이언트에게 반환될 표준 에러 응답 형식을 정의하는 DTO.*/
@Getter
@Builder
public class ErrorResponse {

    /*HTTP 상태 코드 (e.g., 400, 404).*/
    private final int status;

    /*HTTP 상태 이름 (e.g., "BAD_REQUEST", "NOT_FOUND").*/
    private final String error;

    /*클라이언트에게 보여질 구체적인 에러 메시지.*/
    private final String message;

    /**
     * ErrorCode를 기반으로 표준 ResponseEntity<ErrorResponse> 객체를 생성하는 정적 팩토리 메소드.
     * @param errorCode 발생한 예외의 ErrorCode.
     * @return HTTP 상태 코드와 에러 정보가 담긴 ResponseEntity 객체.
     */
    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.builder()
                        .status(errorCode.getHttpStatus().value())
                        .error(errorCode.getHttpStatus().name())
                        .message(errorCode.getMessage())
                        .build()
                );
    }
}