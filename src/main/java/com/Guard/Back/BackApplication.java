// BackApplication.java
package com.Guard.Back;

import jakarta.annotation.PostConstruct; // 👈 import 추가
import java.util.TimeZone; // 👈 import 추가
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackApplication {

	/**
	 * 애플리케이션 실행 전 시간대를 서울(KST)로 설정합니다.
	 */
	@PostConstruct
	public void started() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
	}

	public static void main(String[] args) {
		SpringApplication.run(BackApplication.class, args);
	}
}