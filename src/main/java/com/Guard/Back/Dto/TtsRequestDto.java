package com.Guard.Back.Dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TtsRequestDto {
    private String text; // 음성으로 변환할 텍스트
}