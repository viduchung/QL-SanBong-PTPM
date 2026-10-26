package com.football.bookingservice.service;

import com.football.bookingservice.dto.AuthResponse;
import com.football.bookingservice.dto.LoginRequest;
import com.football.bookingservice.dto.RegisterRequest;
import com.football.bookingservice.dto.UserProfileResponse;
import com.football.bookingservice.model.Role;
import com.football.bookingservice.model.User;
import com.football.bookingservice.repository.UserRepository;
import com.football.bookingservice.security.CustomUserDetails;
import com.football.bookingservice.security.JwtTokenProvider;
import jakarta.annotation.PostConstruct;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @PostConstruct
    @Transactional
    public void initSeedUsers() {
        if (userRepository.count() == 0) {
            userRepository.saveAll(List.of(
                    new User(null, "customer01", passwordEncoder.encode("password123"),
                            Role.ROLE_CUSTOMER, "Nguyen Van An", "0901234567", true),
                    new User(null, "customer02", passwordEncoder.encode("password123"),
                            Role.ROLE_CUSTOMER, "Tran Thi Binh", "0912345678", true),
                    new User(null, "owner01", passwordEncoder.encode("password123"),
                            Role.ROLE_FIELD_OWNER, "Le Chu San", "0988888888", true),
                    new User(null, "admin01", passwordEncoder.encode("password123"),
                            Role.ROLE_ADMIN, "Admin Tong", "0999999999", true)
            ));
        }
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            if (!user.isActive()) {
                throw new BadCredentialsException("Tài khoản đã bị vô hiệu hóa");
            }

            String token = tokenProvider.generateToken(authentication);

            return new AuthResponse(
                    token,
                    "Bearer",
                    tokenProvider.getExpirationSeconds(),
                    user.getUsername(),
                    user.getRole().name(),
                    user.getFullName()
            );
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập '" + request.getUsername() + "' đã tồn tại");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Số điện thoại '" + request.getPhone() + "' đã được đăng ký");
        }

        Role assignedRole = (request.getRole() != null) ? request.getRole() : Role.ROLE_CUSTOMER;

        User newUser = new User(
                null,
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                assignedRole,
                request.getFullName(),
                request.getPhone(),
                true
        );

        User savedUser = userRepository.save(newUser);

        String token = tokenProvider.generateToken(
                savedUser.getUsername(),
                savedUser.getRole().name(),
                savedUser.getFullName()
        );

        return new AuthResponse(
                token,
                "Bearer",
                tokenProvider.getExpirationSeconds(),
                savedUser.getUsername(),
                savedUser.getRole().name(),
                savedUser.getFullName()
        );
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getFullName(),
                user.getPhone(),
                user.isActive()
        );
    }
}
