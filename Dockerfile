# 1. Java 17을 포함한 기본 이미지 사용 (build.gradle 기준)
FROM openjdk:17-jdk-slim

# 2. 빌드된 JAR 파일이 생성될 경로를 변수로 지정
ARG JAR_FILE=build/libs/*.jar

# 3. JAR 파일을 Docker 이미지 내부의 /app.jar로 복사
COPY ${JAR_FILE} /app.jar

# 4. 타임존을 서울로 설정
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 5. 애플리케이션 실행 명령어
ENTRYPOINT ["java", "-jar", "/app.jar"]