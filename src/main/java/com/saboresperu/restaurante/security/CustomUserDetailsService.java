package com.saboresperu.restaurante.security;

import com.saboresperu.restaurante.entity.User;
import com.saboresperu.restaurante.repository.UserRepository;
import com.saboresperu.restaurante.service.interfaces.EffectivePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final EffectivePermissionService effectivePermissionService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailWithRoles(username.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new UsernameNotFoundException("User inactive: " + username);
        }

        Set<String> permCodes = effectivePermissionService.resolvePermissionCodes(user.getId());

        List<GrantedAuthority> authorities = new ArrayList<>();
        user.getRoles().forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r.getName())));
        permCodes.forEach(code -> authorities.add(new SimpleGrantedAuthority("PERM_" + code)));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}