package com.isa.backend.controller;

import com.isa.backend.model.User;
import com.isa.backend.dto.UserLoginDto;
import com.isa.backend.dto.UserRegistrationDto;
import com.isa.backend.dto.UserTokenStateDto;
import com.isa.backend.service.EmailService;
import com.isa.backend.service.UserService;
import com.isa.backend.util.TokenUtils;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RateLimiterRegistry rateLimiterRegistry;

    public AuthenticationController(UserService userService, AuthenticationManager authenticationManager, TokenUtils tokenUtils) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenUtils = tokenUtils;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationDto registerRequest) {
        try {
            userService.save(registerRequest);
            return ResponseEntity.ok("Registration successful!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<UserTokenStateDto> login(@RequestBody UserLoginDto loginRequest, HttpServletRequest request) {
        String ip = request.getRemoteAddr();

        RateLimiter limiter = rateLimiterRegistry.rateLimiter(ip, "ip");
        if (!limiter.acquirePermission()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        User user = userService.findByUsername(loginRequest.getUsername());
        if (user == null) {
            return ResponseEntity.badRequest().body(null);
        }

        if (!user.isEnabled()) {
            throw new RuntimeException("Account not active. Check your email.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        user = (User) authentication.getPrincipal();
        String jwt = tokenUtils.generateToken(user.getUsername());
        int expiresIn = tokenUtils.getExpiredIn();

        return ResponseEntity.ok(new UserTokenStateDto(jwt, expiresIn));
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateUser(@RequestParam("code") String code) {
        User user = userService.findByActivationCode(code);
        if (user == null) {
            return ResponseEntity.badRequest().body("Invalid activation code.");
        }

        user.setEnabled(true);
        user.setActivationCode(null);
        userService.saveActiveUser(user);
        return ResponseEntity.ok("Account is now active.");
    }

    public ResponseEntity<UserTokenStateDto> ipFallback(RequestNotPermitted e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(null);
    }

}