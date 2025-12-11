package com.Guard.Back.Dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GoogleUserInfoResponseDto {
    private String id;
    private String email;
    private boolean verified_email;
    private String name;
    private String picture;
}