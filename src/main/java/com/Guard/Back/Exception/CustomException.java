package com.Guard.Back.Exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 애플리케이션의 비즈니스 로직 예외를 처리하기 위한 커스텀 예외 클래스.
 * 모든 비즈니스 규칙 위반 시 이 예외를 사용하여 일관된 에러 처리를 합니다.
 */
@Getter
@RequiredArgsConstructor
public class CustomException extends RuntimeException {

    /**
     * 발생한 예외의 종류를 정의하는 ErrorCode.
     * HTTP 상태 코드와 에러 메시지를 포함하고 있습니다.
     */
    private final ErrorCode errorCode;
}