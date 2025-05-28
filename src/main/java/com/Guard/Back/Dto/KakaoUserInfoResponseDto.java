package com.Guard.Back.Dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
public class KakaoUserInfoResponseDto {
    private Long id;
    private Map<String, String> properties;  // nickname, profile_image 등
    private Map<String, Object> kakao_account;  // email, age_range 등 (nullable)
}
