package com.sonddd.paywallet.service.impl;

import com.sonddd.paywallet.dto.request.LoginRequest;
import com.sonddd.paywallet.dto.request.RegisterRequest;
import com.sonddd.paywallet.dto.response.AuthResponse;
import com.sonddd.paywallet.dto.response.WalletResponse;
import com.sonddd.paywallet.entity.User;
import com.sonddd.paywallet.entity.Wallet;
import com.sonddd.paywallet.enums.Role;
import com.sonddd.paywallet.enums.WalletStatus;
import com.sonddd.paywallet.exception.DuplicateResourceException;
import com.sonddd.paywallet.exception.ResourceNotFoundException;
import com.sonddd.paywallet.repository.UserRepository;
import com.sonddd.paywallet.repository.WalletRepository;
import com.sonddd.paywallet.security.JwtService;
import com.sonddd.paywallet.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_CURRENCY = "VND";

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                           WalletRepository walletRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public WalletResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email đã được sử dụng");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(Role.USER)
                .enabled(true)
                .build();
        user = userRepository.save(user);

        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO.setScale(2))
                .currency(DEFAULT_CURRENCY)
                .status(WalletStatus.ACTIVE)
                .build();
        wallet = walletRepository.save(wallet);

        return WalletResponse.from(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Ném BadCredentialsException nếu sai thông tin -> GlobalExceptionHandler trả 401
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(token, "Bearer", user.getUsername(),
                user.getRole().name(), jwtService.getExpirationSeconds());
    }
}
