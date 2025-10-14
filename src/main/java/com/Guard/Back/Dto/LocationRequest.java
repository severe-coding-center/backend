package com.Guard.Back.Dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/*피보호자가 자신의 위치 정보를 서버로 업로드할 때 사용하는 데이터 전송 객체(DTO).*/
public record LocationRequest(
        /*
         * 현재 위치의 위도.
         * null일 수 없습니다.
         */
        @NotNull
        Double latitude,

        /*
         * 현재 위치의 경도.
         * null일 수 없습니다.
         */
        @NotNull
        Double longitude,

        /*
         * 위치가 기록된 시간.
         * 클라이언트에서 보내지 않으면 서버에서 현재 시간으로 자동 설정됩니다.
         */
        LocalDateTime recordedAt
) {}