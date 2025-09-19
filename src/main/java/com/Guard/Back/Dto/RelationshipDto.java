package com.Guard.Back.Dto;

/**
 * 관계 설정 관련 요청을 위한 데이터 전송 객체(DTO)들을 포함합니다.
 */
public class RelationshipDto {
    /**
     * 보호자가 피보호자와 연동하기 위해 보내는 요청 DTO.
     * @param linkingCode 피보호자에게 발급된 6자리 연동 코드
     */
    public record LinkRequest(String linkingCode) {}
}