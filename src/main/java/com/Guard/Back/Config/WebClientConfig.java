package com.Guard.Back.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/*외부 API와의 비동기 HTTP 통신을 위한 WebClient를 설정하는 클래스.*/
@Configuration
public class WebClientConfig {

    /**
     * 애플리케이션 전역에서 사용될 WebClient Bean을 생성
     * 이 Bean은 다른 서비스(@Service) 클래스에 주입되어 사용
     *
     * @return 기본 설정으로 생성된 WebClient 객체.
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}