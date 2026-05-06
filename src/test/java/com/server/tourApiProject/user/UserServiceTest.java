package com.server.tourApiProject.user;

import com.server.tourApiProject.bigPost.post.Post;
import com.server.tourApiProject.fcm.FcmService;
import com.server.tourApiProject.myWish.MyWishRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MyWishRepository myWishRepository;

    @Mock
    private UserPasswordService userPasswordService;

    @Mock
    private FcmService fcmService;

    @Mock
    private PasswordEncoder bCryptPasswordEncoder;

    @Mock
    private JavaMailSender javaMailSender;

    @Spy
    @InjectMocks
    private UserService userService;

    @Test
    void getUser_returnsPersistedUser() {
        User user = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUser(1L);

        assertSame(user, result);
    }

    @Test
    void getUser_throwsWhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalAccessError.class, () -> userService.getUser(1L));
    }

    @Test
    void getUser2_mapsNicknameAndProfileImage() {
        User user = user(2L);
        user.setNickName("stargazer");
        user.setProfileImage("profile.png");
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        UserParams2 result = userService.getUser2(2L);

        assertEquals("stargazer", result.getNickName());
        assertEquals("profile.png", result.getProfileImage());
    }

    @Test
    void createUser_hashesPasswordAndPersistsGeneratedNickname() {
        UserParams userParams = UserParams.builder()
                .realName("Kim")
                .sex(true)
                .birthDay("1990-01-01")
                .mobilePhoneNumber("01012345678")
                .email("kim@test.com")
                .password("plain")
                .isMarketing(true)
                .kakao(false)
                .build();
        when(userPasswordService.hashPassword(bCryptPasswordEncoder, "plain")).thenReturn("hashed");
        when(userRepository.findByNickName(anyString())).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setUserId(77L);
            return saved;
        });

        String result = userService.createUser(userParams);

        assertEquals("77", result);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("Kim", captor.getValue().getRealName());
        assertEquals("hashed", captor.getValue().getEncryptedPassword());
        assertEquals("kim@test.com", captor.getValue().getEmail());
        assertNotNull(captor.getValue().getNickName());
        assertNotNull(captor.getValue().getSignUpDt());
    }

    @Test
    void createKakaoUser_setsOnlyProvidedOptionalFields() {
        KakaoUserParams params = KakaoUserParams.builder()
                .email("kakao@test.com")
                .nickName("kakao-user")
                .profileImage("kakao.png")
                .mobilePhoneNumber("01098765432")
                .build();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setUserId(9L);
            return saved;
        });

        String result = userService.createKakaoUser(params);

        assertEquals("9", result);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("kakao@test.com", captor.getValue().getEmail());
        assertEquals("kakao-user", captor.getValue().getNickName());
        assertEquals("kakao.png", captor.getValue().getProfileImage());
        assertTrue(captor.getValue().getKakao());
        assertEquals("01098765432", captor.getValue().getMobilePhoneNumber());
        assertEquals(null, captor.getValue().getSex());
    }

    @Test
    void logIn_returnsMinusOneWhenUserDoesNotExist() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(null);

        Long result = userService.logIn("none@test.com", "pw");

        assertEquals(-1L, result);
    }

    @Test
    void logIn_returnsMinusTwoForKakaoUser() {
        User user = user(3L);
        user.setKakao(true);
        when(userRepository.findByEmail("kakao@test.com")).thenReturn(user);

        Long result = userService.logIn("kakao@test.com", "pw");

        assertEquals(-2L, result);
    }

    @Test
    void logIn_usesLegacyPasswordWhenEncryptedPasswordIsMissing() {
        User user = user(4L);
        user.setKakao(false);
        user.setPassword("plain");
        user.setEncryptedPassword(null);
        when(userRepository.findByEmail("legacy@test.com")).thenReturn(user);

        Long result = userService.logIn("legacy@test.com", "plain");

        assertEquals(4L, result);
    }

    @Test
    void logIn_usesPasswordServiceWhenEncryptedPasswordExists() {
        User user = user(5L);
        user.setKakao(false);
        user.setEncryptedPassword("hashed");
        when(userRepository.findByEmail("secure@test.com")).thenReturn(user);
        when(userPasswordService.checkPassword(bCryptPasswordEncoder, "pw", "hashed")).thenReturn(true);

        Long result = userService.logIn("secure@test.com", "pw");

        assertEquals(5L, result);
    }

    @Test
    void logIn_returnsMinusOneWhenEncryptedPasswordDoesNotMatch() {
        User user = user(6L);
        user.setKakao(false);
        user.setEncryptedPassword("hashed");
        when(userRepository.findByEmail("secure@test.com")).thenReturn(user);
        when(userPasswordService.checkPassword(bCryptPasswordEncoder, "wrong", "hashed")).thenReturn(false);

        Long result = userService.logIn("secure@test.com", "wrong");

        assertEquals(-1L, result);
    }

    @Test
    void getEmail_returnsEmailOnlyWhenNameMatches() {
        User user = user(7L);
        user.setRealName("Lee");
        user.setEmail("lee@test.com");
        when(userRepository.findByMobilePhoneNumber("01011112222")).thenReturn(user);

        assertEquals("lee@test.com", userService.getEmail("Lee", "01011112222"));
        assertEquals("none", userService.getEmail("Park", "01011112222"));
    }

    @Test
    void getPassword_returnsFalseWhenIdentityDoesNotMatch() {
        User user = user(8L);
        user.setRealName("Han");
        user.setMobilePhoneNumber("01022223333");
        when(userRepository.findByEmail("han@test.com")).thenReturn(user);

        Boolean result = userService.getPassword("han@test.com", "Kim", "01022223333");

        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getPassword_updatesEncryptedPasswordAndSendsTemporaryPassword() {
        User user = user(9L);
        user.setRealName("Han");
        user.setMobilePhoneNumber("01022223333");
        when(userRepository.findByEmail("han@test.com")).thenReturn(user);
        doReturn("TEMP1234").when(userService).getTmpPassword(8);
        when(userPasswordService.hashPassword(bCryptPasswordEncoder, "TEMP1234")).thenReturn("hashed-temp");
        doReturn(true).when(userService).sendTmpPassword("han@test.com", "TEMP1234");

        Boolean result = userService.getPassword("han@test.com", "Han", "01022223333");

        assertTrue(result);
        assertEquals("hashed-temp", user.getEncryptedPassword());
        verify(userRepository).save(user);
    }

    @Test
    void sendTmpPassword_returnsTrueWhenMailIsSent() {
        Boolean result = userService.sendTmpPassword("mail@test.com", "TEMP1234");

        assertTrue(result);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(captor.capture());
        assertEquals("mail@test.com", captor.getValue().getTo()[0]);
        assertTrue(captor.getValue().getText().contains("TEMP1234"));
    }

    @Test
    void sendTmpPassword_returnsFalseWhenMailSenderThrows() {
        doThrow(new MailSendException("fail")).when(javaMailSender).send(any(SimpleMailMessage.class));

        Boolean result = userService.sendTmpPassword("mail@test.com", "TEMP1234");

        assertFalse(result);
    }

    @Test
    void changePassword_returnsFalseWhenOriginPasswordDoesNotMatch() {
        User user = user(10L);
        user.setEncryptedPassword("current");
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(userPasswordService.checkPassword(bCryptPasswordEncoder, "wrong", "current")).thenReturn(false);

        Boolean result = userService.changePassword(10L, "wrong", "new");

        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_rehashesAndSavesWhenOriginPasswordMatches() {
        User user = user(11L);
        user.setEncryptedPassword("current");
        when(userRepository.findById(11L)).thenReturn(Optional.of(user));
        when(userPasswordService.checkPassword(bCryptPasswordEncoder, "old", "current")).thenReturn(true);
        when(userPasswordService.hashPassword(bCryptPasswordEncoder, "new")).thenReturn("new-hash");

        Boolean result = userService.changePassword(11L, "old", "new");

        assertTrue(result);
        assertEquals("new-hash", user.getEncryptedPassword());
        verify(userRepository).save(user);
    }

    @Test
    void deleteUser_removesWishesForEachPostThenDeletesUser() {
        Post first = Post.builder().postId(100L).build();
        Post second = Post.builder().postId(101L).build();
        User user = user(12L);
        user.setMyPosts(List.of(first, second));
        when(userRepository.findById(12L)).thenReturn(Optional.of(user));

        userService.deleteUser(12L);

        verify(myWishRepository).deleteByItemIdAndWishType(100L, 2);
        verify(myWishRepository).deleteByItemIdAndWishType(101L, 2);
        verify(userRepository).deleteById(12L);
    }

    @Test
    void checkIsKakao_returnsStoredFlag() {
        User user = user(13L);
        user.setKakao(true);
        when(userRepository.findById(13L)).thenReturn(Optional.of(user));

        assertTrue(userService.checkIsKakao(13L));
    }

    @Test
    void encodePassword_onlyEncodesUsersMissingEncryptedPassword() {
        User needsEncoding = user(14L);
        needsEncoding.setPassword("plain");
        needsEncoding.setEncryptedPassword(null);
        User alreadyEncoded = user(15L);
        alreadyEncoded.setPassword("plain");
        alreadyEncoded.setEncryptedPassword("hashed");
        User noPassword = user(16L);
        noPassword.setPassword(null);
        noPassword.setEncryptedPassword(null);
        when(userRepository.findAll()).thenReturn(List.of(needsEncoding, alreadyEncoded, noPassword));
        when(userPasswordService.hashPassword(bCryptPasswordEncoder, "plain")).thenReturn("hashed-new");

        userService.encodePassword();

        assertEquals("hashed-new", needsEncoding.getEncryptedPassword());
        verify(userRepository, times(1)).save(needsEncoding);
        verify(userRepository, never()).save(alreadyEncoded);
        verify(userRepository, never()).save(noPassword);
    }

    @Test
    void getNickName_returnsNicknameMap() {
        User user = user(17L);
        user.setNickName("nova");
        when(userRepository.findById(17L)).thenReturn(Optional.of(user));

        assertEquals("nova", userService.getNickName(17L).get("nickName"));
    }

    private User user(Long userId) {
        User user = new User();
        user.setUserId(userId);
        user.setEmail("user" + userId + "@test.com");
        user.setNickName("nick-" + userId);
        user.setKakao(false);
        user.setMyPosts(new java.util.ArrayList<>());
        user.setSignUpDt(LocalDateTime.now());
        return user;
    }
}
