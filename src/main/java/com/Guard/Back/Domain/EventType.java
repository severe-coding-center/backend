package com.Guard.Back.Domain;

public enum EventType {
    SOS,             // SOS 호출
    GEOFENCE_EXIT,   // 지오펜스 이탈
    GEOFENCE_ENTER   // 지오펜스 진입
}