package com.sonddd.paywallet.security;

import com.sonddd.paywallet.entity.User;
import com.sonddd.paywallet.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Nạp thông tin người dùng từ CSDL cho Spring Security.
 * Chuyển {@link User} (entity) thành {@link UserDetails} với quyền ROLE_*.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .disabled(!user.isEnabled())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .build();
    }
}
