package com.Guard.Back.Dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

public class LocationLogDto {

    /** 위치 정보를 업로드할 때 사용하는 DTO */
    @Getter
    @NoArgsConstructor
    public static class LocationRequest {
        private double latitude;
        private double longitude;
        private LocalDateTime recordedAt;
    }

    /** 위치 정보를 조회할 때 응답으로 보내주는 DTO */
    @Getter
    public static class LocationResponse {
        private final double latitude;
        private final double longitude;
        private final LocalDateTime recordedAt;

        public LocationResponse(double latitude, double longitude, LocalDateTime recordedAt) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.recordedAt = recordedAt;
        }
    }
}