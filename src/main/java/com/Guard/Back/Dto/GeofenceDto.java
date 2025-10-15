package com.Guard.Back.Dto;

import jakarta.validation.constraints.NotNull;

public record GeofenceDto(
        @NotNull Double latitude,
        @NotNull Double longitude,
        @NotNull Integer radius // 미터 단위
) {}