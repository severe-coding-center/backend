package com.Guard.Back.Dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Map;

@Getter
@NoArgsConstructor
public class KakaoUserInfoResponseDto {
    private Long id;
    private Map<String, String> properties;
}