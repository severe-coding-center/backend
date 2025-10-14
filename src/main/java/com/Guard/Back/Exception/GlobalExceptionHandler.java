package com.Guard.Back.Exception;

import lombok.extern.slf4j.Slf4j; // 💡 Slf4j 임포트 추가
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 애플리케이션 전역에서 발생하는 예외를 처리하는 클래스.
 * @RestControllerAdvice 어노테이션을 통해 모든 @RestController에서 발생하는 예외를 가로챕니다.
 */
@RestControllerAdvice
@Slf4j // 💡 로깅을 위한 어노테이션 추가
public class GlobalExceptionHandler {

    /**
     * CustomException 타입의 예외가 발생했을 때 처리하는 핸들러 메소드.
     *
     * @param e 발생한 CustomException 객체.
     * @return ErrorCode에 정의된 HTTP 상태 코드와 메시지를 담은 ResponseEntity<ErrorResponse> 객체.
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("비즈니스 로직 예외가 발생했습니다. 에러 코드: {}, 메시지: {}",
                errorCode.name(), errorCode.getMessage());
        return ErrorResponse.toResponseEntity(errorCode);
    }
}