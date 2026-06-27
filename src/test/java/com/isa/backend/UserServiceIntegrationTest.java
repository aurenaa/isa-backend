package com.isa.backend;

import com.isa.backend.dto.UserRegistrationDto;
import com.isa.backend.model.User;
import com.isa.backend.repository.UserRepository;
import com.isa.backend.service.EmailService;
import com.isa.backend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class UserServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserServiceImpl userService;

    @MockBean
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void test_create_user_success_integration() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("testuser");
        dto.setPassword("plain_password");
        dto.setFirstname("Test");
        dto.setLastname("User");
        dto.setEmail("test@example.com");

        User result = userService.save(dto);

        assertNotNull(result);

        User userInDb = userRepository.findByUsername("testuser");
        assertNotNull(userInDb);
        assertThat(userInDb.getUsername()).isEqualTo("testuser");

        verify(emailService, times(1)).sendActivationEmail(any(User.class));
    }

    @Test
    void test_create_user_already_exists_exception_integration() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("hashed_password");
        user.setEmail("test@example.com");
        userRepository.save(user);

        UserRegistrationDto duplicateDto = new UserRegistrationDto();
        duplicateDto.setUsername("testuser");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.save(duplicateDto);
        });

        assertThat(exception.getMessage()).isEqualTo("Username already exists!");
    }

    @Test
    void test_load_user_by_username_not_found_exception_integration() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.loadUserByUsername("testuser");
        });

        assertThat(exception.getMessage()).isEqualTo("No user found with username 'testuser'.");
    }
}