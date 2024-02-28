package com.eturn.eturn.security;

import com.eturn.eturn.dto.UserCreateDTO;
import com.eturn.eturn.dto.mapper.UserMapper;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.RoleEnum;
import com.eturn.eturn.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserService userService, JwtService jwtService, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public JwtAuthenticationResponse signUp(UserCreateDTO userCreateDTO) {

//        com.eturn.eturn.entity.User user = User.builder()
//                .username(userCreateDTO.login())
//                .password(passwordEncoder.encode(userCreateDTO.password()))
//                .roles(userCreateDTO.role())
//                .build();
        RoleEnum r = RoleEnum.valueOf(userCreateDTO.role());
        String password = passwordEncoder.encode(userCreateDTO.password());
        UserCreateDTO newDtoUser = new UserCreateDTO(
                userCreateDTO.name(),
                userCreateDTO.role(),
                userCreateDTO.facultyId(),
                userCreateDTO.groupId(),
                userCreateDTO.courseId(),
                userCreateDTO.departmentId(),
                userCreateDTO.login(),
                password
        );
        long id = userService.createUser(newDtoUser);
        User user = userService.getUserFrom(id);

        var jwt = "Bearer "+jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }

    /**
     * Аутентификация пользователя
     *
     *
     * @return токен
     */
    public JwtAuthenticationResponse signIn(String login, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                login,
                password
        ));

        var user = userService
                .userDetailsService()
                .loadUserByUsername(login);

        var jwt = "Bearer "+jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }
}
