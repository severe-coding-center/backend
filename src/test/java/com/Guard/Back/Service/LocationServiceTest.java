package com.Guard.Back.Service;

import com.Guard.Back.Domain.LocationLog;
import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Domain.User;
import com.Guard.Back.Dto.LocationResponse;
import com.Guard.Back.Exception.CustomException;
import com.Guard.Back.Repository.LocationLogRepository;
import com.Guard.Back.Repository.ProtectedUserRepository;
import com.Guard.Back.Repository.RelationshipRepository;
import com.Guard.Back.Repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @InjectMocks
    private LocationService locationService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private ProtectedUserRepository protectedUserRepository;
    @Mock
    private RelationshipRepository relationshipRepository;
    @Mock
    private LocationLogRepository locationLogRepository;

    @Test
    @DisplayName("위치 조회 성공 - 보호자와 피보호자가 정상적으로 연결된 경우")
    void getLatestLocation_Success() {
        // given
        User guardian = new User(1L, null, "보호자", null, null, "g1");
        ProtectedUser protectedUser = new ProtectedUser(2L, "p1", "code");

        when(userRepository.findById(1L)).thenReturn(Optional.of(guardian));
        when(protectedUserRepository.findById(2L)).thenReturn(Optional.of(protectedUser));
        when(relationshipRepository.existsByGuardianAndProtectedUser(guardian, protectedUser)).thenReturn(true);
        LocationLog fakeLog = LocationLog.builder().latitude(37.5).longitude(127.0).recordedAt(LocalDateTime.now()).build();
        when(locationLogRepository.findTopByProtectedUserOrderByIdDesc(protectedUser)).thenReturn(Optional.of(fakeLog));

        // when
        LocationResponse response = locationService.getLatestLocation(2L, 1L);

        // then
        assertNotNull(response);
        assertEquals(37.5, response.latitude());
        verify(relationshipRepository, times(1)).existsByGuardianAndProtectedUser(guardian, protectedUser);
    }

    @Test
    @DisplayName("위치 조회 실패 - 관계가 없는 경우 CustomException 발생")
    void getLatestLocation_Fail_NoRelationship() {
        // given
        User guardian = new User(1L, null, "보호자", null, null, "g1");
        ProtectedUser protectedUser = new ProtectedUser(2L, "p1", "code");

        when(userRepository.findById(1L)).thenReturn(Optional.of(guardian));
        when(protectedUserRepository.findById(2L)).thenReturn(Optional.of(protectedUser));
        when(relationshipRepository.existsByGuardianAndProtectedUser(guardian, protectedUser)).thenReturn(false);

        // when & then
        assertThrows(CustomException.class, () -> {
            locationService.getLatestLocation(2L, 1L);
        });
        verify(locationLogRepository, never()).findTopByProtectedUserOrderByIdDesc(any());
    }
}