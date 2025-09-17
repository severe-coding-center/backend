package com.Guard.Back.Service;

import com.Guard.Back.Domain.User;
import com.Guard.Back.Domain.UserType;
import com.Guard.Back.Dto.AuthDto.SignUpRequest;
import com.Guard.Back.Jwt.JwtTokenProvider;
import com.Guard.Back.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();

    // 회원가입
    public void signUp(SignUpRequest request) {
        if (userRepository.findByPhoneNumber(request.phoneNumber()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 휴대폰 번호입니다.");
        }

        String newLinkingCode = null;
        if (request.userType() == UserType.PROTECTED) {
            newLinkingCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }

        User newUser = User.builder()
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .userType(request.userType())
                .linkingCode(newLinkingCode)
                .build();

        userRepository.save(newUser);
    }

    // 인증번호 발송
    public void sendVerificationCode(String phoneNumber) {
        userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 번호입니다."));

        String code = String.valueOf((int) (Math.random() * 899999) + 100000);
        System.out.println("인증코드 [" + code + "]를 " + phoneNumber + "로 발송했습니다.");
        verificationCodes.put(phoneNumber, code);
    }

    // 인증번호 검증 및 로그인
    public String verifyCodeAndLogin(String phoneNumber, String code) {
        String storedCode = verificationCodes.get(phoneNumber);
        if (storedCode == null || !storedCode.equals(code)) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        }
        verificationCodes.remove(phoneNumber);

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalStateException("인증 후 사용자를 찾을 수 없습니다."));

        return jwtTokenProvider.createAccessToken(user.getId(), user.getName());
    }
}