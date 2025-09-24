package com.Guard.Back.Service;

import com.Guard.Back.Domain.User;
import com.Guard.Back.Dto.AuthDto.*;
import com.Guard.Back.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // 임시 인증 코드 저장을 위한 맵 (실제 서비스에서는 Redis 사용 권장)
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();

    /**
     * 💡 [신규] 1. 회원가입을 위한 인증번호를 발송합니다.
     */
    public void sendVerificationCode(PhoneRequest request) {
        String phoneNumber = request.phoneNumber();
        if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 휴대폰 번호입니다.");
        }
        String code = String.valueOf((int) (Math.random() * 899999) + 100000);
        System.out.println("인증코드 [" + code + "]를 " + phoneNumber + "로 발송했습니다. (실제 SMS 로직 필요)");
        verificationCodes.put(phoneNumber, code);
    }

    /**
     * 💡 [신규] 2. 사용자가 입력한 인증번호를 검증합니다.
     */
    public boolean verifyCode(VerificationRequest request) {
        String storedCode = verificationCodes.get(request.phoneNumber());
        // 코드가 일치하면 true, 아니면 false 반환
        boolean isValid = storedCode != null && storedCode.equals(request.code());
        if (isValid) {
            verificationCodes.remove(request.phoneNumber()); // 검증 완료 후 코드 삭제
        }
        return isValid;
    }

    /**
     * 💡 [수정] 3. 최종 회원가입을 처리합니다.
     */
    @Transactional
    public void signUp(SignUpRequest request) {
        // 이 단계에서는 전화번호 중복 검사를 다시 하지 않습니다 (이미 인증 요청 시 확인)
        User newUser = User.builder()
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .password(passwordEncoder.encode(request.password())) // 사용자가 직접 만든 비밀번호를 암호화
                .build();
        userRepository.save(newUser);
    }


    /**
     * 보호자 로그인을 처리합니다.
     */
    public User login(LoginRequest request) {
        User user = userRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 번호입니다."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return user;
    }
}