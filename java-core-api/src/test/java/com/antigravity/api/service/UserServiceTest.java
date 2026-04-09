package com.antigravity.api.service;

import com.antigravity.api.dto.GoogleLoginRequestDto;
import com.antigravity.api.dto.SocialLoginRequestDto;
import com.antigravity.api.entity.User;
import com.antigravity.api.repository.PortfolioRepository;
import com.antigravity.api.repository.TradeHistoryRepository;
import com.antigravity.api.repository.UserRepository;
import com.antigravity.api.repository.WatchlistRepository;
import com.antigravity.api.service.impl.UserServiceImpl;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PortfolioRepository portfolioRepository;
    @Mock
    private WatchlistRepository watchlistRepository;
    @Mock
    private TradeHistoryRepository tradeHistoryRepository;
    @Mock
    private FirebaseAuthService firebaseAuthService;
    @Mock
    private GoogleAuthService googleAuthService;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .firebaseUid("firebase-uid-123")
                .isActive(true)
                .build();
    }

    @Test
    void getUserByFirebaseUid_ShouldReturnUser_WhenExists() {
        when(userRepository.findByFirebaseUid("firebase-uid-123")).thenReturn(Optional.of(mockUser));

        User result = userService.getUserByFirebaseUid("firebase-uid-123");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).findByFirebaseUid("firebase-uid-123");
    }

    @Test
    void getUserByFirebaseUid_ShouldThrowException_WhenNotExists() {
        when(userRepository.findByFirebaseUid("non-existent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.getUserByFirebaseUid("non-existent"));
    }

    @Test
    void loginWithGoogle_ShouldReturnExistingUser() throws Exception {
        GoogleLoginRequestDto dto = new GoogleLoginRequestDto();
        dto.setIdToken("google-token");

        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail("test@example.com");
        payload.set("name", "Test User");
        payload.set("picture", "http://picture.url");
        payload.setSubject("google-sub-id");

        when(googleAuthService.verifyIdToken("google-token")).thenReturn(payload);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        User result = userService.loginWithGoogle(dto);

        assertNotNull(result);
        verify(googleAuthService).verifyIdToken("google-token");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void loginWithSocial_Firebase_ShouldCreateNewUser() throws Exception {
        SocialLoginRequestDto dto = SocialLoginRequestDto.builder()
                .idToken("firebase-token")
                .platform("FIREBASE")
                .build();

        FirebaseToken mockFirebaseToken = mock(FirebaseToken.class);
        when(mockFirebaseToken.getEmail()).thenReturn("new@example.com");
        when(mockFirebaseToken.getUid()).thenReturn("fb-uid-new");
        when(mockFirebaseToken.getClaims()).thenReturn(Map.of("name", "New User"));

        when(firebaseAuthService.verifyIdToken("firebase-token")).thenReturn(mockFirebaseToken);
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.loginWithSocial(dto);

        assertNotNull(result);
        assertEquals("new@example.com", result.getEmail());
        assertEquals("New User", result.getFullName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser_ShouldCallAllDependencies() {
        userService.deleteUser(mockUser);

        verify(tradeHistoryRepository).deleteByUser(mockUser);
        verify(watchlistRepository).deleteByUser(mockUser);
        verify(portfolioRepository).deleteByUser(mockUser);
        verify(userRepository).delete(mockUser);
    }

    @Test
    void updatePushToken_ShouldUpdateWhenDifferent() {
        userService.updatePushToken(mockUser, "new-token");

        assertEquals("new-token", mockUser.getPushToken());
        verify(userRepository).save(mockUser);
    }
}
