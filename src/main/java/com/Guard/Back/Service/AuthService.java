package com.Guard.Back.Service;

import com.Guard.Back.Dto.AuthDto.LoginRequest;
import com.Guard.Back.Domain.User;
import com.Guard.Back.Dto.AuthDto.SignUpRequest;
import com.Guard.Back.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 보호자(User)의 회원가입 및 로그인 비즈니스 로직을 처리하는 서비스 클래스.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 보호자 회원가입을 처리합니다.
     * @param request 회원가입 정보(이름, 전화번호)
     * @return 생성된 8자리 보안 비밀번호 (실제 서비스에서는 SMS로 발송)
     */
    @Transactional
    public String signUp(SignUpRequest request) {
        // 전화번호 중복 가입 방지
        if (userRepository.findByPhoneNumber(request.phoneNumber()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 휴대폰 번호입니다.");
        }

        // 💡 강력한 초기 비밀번호 생성
        String initialPassword = generateSecurePassword();

        System.out.println("보호자 [" + request.name() + "] 님의 초기 비밀번호: " + initialPassword);

        User newUser = User.builder()
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                // 비밀번호는 반드시 BCrypt로 해싱하여 저장
                .password(passwordEncoder.encode(initialPassword))
                .build();
        userRepository.save(newUser);

        return initialPassword;
    }

    /**
     * 보호자 로그인을 처리합니다.
     * @param request 로그인 정보(전화번호, 비밀번호)
     * @return 인증에 성공한 User 객체
     * @throws IllegalArgumentException 전화번호가 존재하지 않거나 비밀번호가 틀린 경우
     */
    public User login(LoginRequest request) {
        User user = userRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 번호입니다."));

        // 입력된 비밀번호와 DB에 저장된 해시값을 비교
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return user;
    }

    /**
     * 8자리 보안 비밀번호를 생성하는 private 메소드.
     * 영문 대문자, 소문자, 숫자, 특수문자가 최소 1개 이상 포함됩니다.
     * @return 생성된 8자리 비밀번호
     */
    private String generateSecurePassword() {
        final String charsLower = "abcdefghijklmnopqrstuvwxyz";
        final String charsUpper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String numbers = "0123456789";
        final String specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?";

        SecureRandom random = new SecureRandom();
        List<Character> passwordChars = new ArrayList<>();

        // 1. 각 종류의 문자에서 최소 1개씩 무작위로 추가
        passwordChars.add(charsLower.charAt(random.nextInt(charsLower.length())));
        passwordChars.add(charsUpper.charAt(random.nextInt(charsUpper.length())));
        passwordChars.add(numbers.charAt(random.nextInt(numbers.length())));
        passwordChars.add(specialChars.charAt(random.nextInt(specialChars.length())));

        // 2. 나머지 4자리를 모든 문자 종류에서 무작위로 채움
        String allChars = charsLower + charsUpper + numbers + specialChars;
        for (int i = 0; i < 4; i++) {
            passwordChars.add(allChars.charAt(random.nextInt(allChars.length())));
        }

        // 3. 생성된 8개의 문자 리스트를 무작위로 섞음
        Collections.shuffle(passwordChars);

        // 4. 최종 비밀번호 문자열로 변환
        StringBuilder password = new StringBuilder();
        for (Character ch : passwordChars) {
            password.append(ch);
        }

        return password.toString();
    }
}