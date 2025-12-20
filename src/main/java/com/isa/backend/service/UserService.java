package com.isa.backend.service;

import com.isa.backend.dto.UserRegistrationDto;
import com.isa.backend.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    User findById(Long id);
    User findByUsername(String username);
    User findByActivationCode(String code);
    List<User> findAll ();
    User save(UserRegistrationDto userRequest);
    User saveActiveUser(User user);
}
