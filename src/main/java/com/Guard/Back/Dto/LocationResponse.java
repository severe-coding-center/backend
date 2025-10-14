package com.Guard.Back.Dto;

import java.time.LocalDateTime;

/**
 * 보호자가 피보호자의 최신 위치를 조회했을 때, 서버가 응답하는 데이터 전송 객체(DTO).
 */
public record LocationResponse(
        /**
         * 조회된 최신 위치의 위도.
         */
        double latitude,

        /**
         * 조회된 최신 위치의 경도.
         */
        double longitude,

        /**
         * 해당 위치가 기록된 시간.
         */
        LocalDateTime recordedAt
) {}