package com.eturn.eturn.security;

import com.eturn.eturn.entity.User;
import com.eturn.eturn.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;


    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByLogin(username);
        if (user == null){
            throw new UsernameNotFoundException("Username or password not found");
        }

        return org.springframework.security.core.userdetails.User.builder().username(user.getLogin()).password(user.getPassword()).authorities(authorities()).accountExpired(true).accountLocked(true).credentialsExpired(true).build();
    }

    public Collection<? extends GrantedAuthority> authorities(){
        return List.of(new SimpleGrantedAuthority("USER"));
    }
}
