package com.Guard.Back.Exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 400 BAD_REQUEST: 잘못된 요청
    INVALID_LINKING_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 연동 코드입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 리프레시 토큰입니다."),

    // 401 UNAUTHORIZED: 인증되지 않은 사용자
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "해당 정보에 접근할 권한이 없습니다."),

    // 404 NOT_FOUND: 리소스를 찾을 수 없음
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    GUARDIAN_NOT_FOUND(HttpStatus.NOT_FOUND, "보호자 정보를 찾을 수 없습니다."),
    PROTECTED_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "피보호자 정보를 찾을 수 없습니다."),
    RELATIONSHIP_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 관계입니다."),

    // 409 CONFLICT: 리소스 충돌
    RELATIONSHIP_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 연결된 관계입니다."),
    MAX_GUARDIANS_REACHED(HttpStatus.CONFLICT, "이미 최대 2명의 보호자가 연결되어 있습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}