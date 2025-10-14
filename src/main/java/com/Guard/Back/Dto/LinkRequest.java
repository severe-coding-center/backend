package com.Guard.Back.Dto;

import jakarta.validation.constraints.NotBlank;
public record LinkRequest(@NotBlank String linkingCode) {} // 💡 @NotBlank 추가