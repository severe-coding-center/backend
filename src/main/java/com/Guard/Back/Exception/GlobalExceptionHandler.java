package com.Guard.Back.Exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException; // 👈 import 추가
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 우리가 직접 정의한 비즈니스 예외(CustomException)를 처리합니다.
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("비즈니스 로직 예외 발생: {}", errorCode.getMessage());
        return ErrorResponse.toResponseEntity(errorCode);
    }

    /**
     * /@Valid 어노테이션을 통한 유효성 검사 실패 시 발생하는 예외를 처리합니다.
     * 주로 클라이언트가 잘못된 형식의 데이터를 보냈을 때 발생합니다. (400 Bad Request)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        // 유효성 검사 실패 시 첫 번째 에러 메시지를 사용합니다.
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("유효성 검사 실패: {}", errorMessage);

        // ErrorResponse DTO를 직접 생성하여 400 상태 코드와 함께 반환합니다.
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.name())
                .message(errorMessage)
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 위에서 처리하지 못한 모든 예외를 처리하는 최후의 보루 역할을 합니다.
     * 주로 서버 내부의 예상치 못한 오류(NullPointerException 등) 발생 시 호출됩니다. (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception e) {
        // 예상치 못한 오류이므로 error 레벨로 로그를 남기고, 스택 트레이스를 함께 기록해야 원인 파악
        log.error("처리되지 않은 예외가 발생했습니다.", e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .message("서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요.")
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}