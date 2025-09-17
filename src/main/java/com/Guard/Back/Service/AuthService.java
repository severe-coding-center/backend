package com.Guard.Back.Service;

import com.Guard.Back.Jwt.JwtTokenProvider; // Jwt 패키지는 곧 만들 예정
import com.Guard.Back.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider; // Jwt 패키지는 곧 만들 예정
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();

    public void sendVerificationCode(String phoneNumber) {
        // 회원가입 전제라면 DB에서 유저를 찾을 필요 없음.
        // 여기서는 가입된 유저만 로그인 가능하다고 가정.
        userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 번호입니다."));

        String code = String.valueOf((int) (Math.random() * 899999) + 100000);
        System.out.println("인증코드 [" + code + "]를 " + phoneNumber + "로 발송했습니다. (실제 SMS 로직 필요)");
        verificationCodes.put(phoneNumber, code);
    }

    public String verifyCodeAndLogin(String phoneNumber, String code) {
        String storedCode = verificationCodes.get(phoneNumber);
        if (storedCode == null || !storedCode.equals(code)) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        }
        verificationCodes.remove(phoneNumber);

        // 전화번호로 유저 정보를 다시 조회
        var user = userRepository.findByPhoneNumber(phoneNumber).orElseThrow();

        // JWT 토큰 생성
        return jwtTokenProvider.createAccessToken(user.getId(), user.getName());
    }
}