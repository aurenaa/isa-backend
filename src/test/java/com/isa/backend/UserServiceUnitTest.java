package com.isa.backend;

import com.isa.backend.dto.UserRegistrationDto;
import com.isa.backend.model.Role;
import com.isa.backend.model.User;
import com.isa.backend.repository.UserRepository;
import com.isa.backend.service.EmailService;
import com.isa.backend.service.RoleService;
import com.isa.backend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {
    @Mock
    private UserRepository mock_db;

    @Mock
    private PasswordEncoder mock_hash;

    @Mock
    private RoleService roleService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void test_create_user_success() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("testuser");
        dto.setPassword("plain_password");
        dto.setFirstname("Test");
        dto.setLastname("User");
        dto.setEmail("test@example.com");

        User savedUser = new User();
        savedUser.setUsername("testuser");

        when(mock_db.findByUsername("testuser")).thenReturn(null);
        when(mock_hash.encode("plain_password")).thenReturn("hashed_password");

        List<Role> mockRoles = new ArrayList<>();
        when(roleService.findByName("ROLE_USER")).thenReturn(mockRoles);
        when(mock_db.save(any(User.class))).thenReturn(savedUser);

        User result = userService.save(dto);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());

        verify(mock_hash, times(1)).encode("plain_password");
        verify(mock_db, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendActivationEmail(any(User.class));
    }

    @Test
    public void test_create_user_already_exists_exception() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("testuser");

        when(mock_db.findByUsername("testuser")).thenReturn(new User());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.save(dto);
        });

        assertEquals("Username already exists!", exception.getMessage());

        verify(mock_db, never()).save(any(User.class));
        verify(emailService, never()).sendActivationEmail(any(User.class));
    }

    @Test
    public void test_load_user_by_username_not_found_exception() {
        when(mock_db.findByUsername("testuser")).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.loadUserByUsername("testuser");
        });

        assertEquals("No user found with username 'testuser'.", exception.getMessage());
    }
}
