package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.UserCreateDTO;
import com.eturn.eturn.dto.UserDTO;
import com.eturn.eturn.dto.mapper.UserMapper;
import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.RoleEnum;
import com.eturn.eturn.exception.user.AuthPasswordException;
import com.eturn.eturn.exception.user.LocalNotFoundUserException;
import com.eturn.eturn.exception.user.NotFoundUserException;
import com.eturn.eturn.repository.UserRepository;
import com.eturn.eturn.service.FacultyService;
import com.eturn.eturn.service.GroupService;
import com.eturn.eturn.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FacultyService facultyService;
    private final GroupService groupService;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, FacultyService facultyService,
                           GroupService groupService, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.facultyService = facultyService;
        this.groupService = groupService;
        this.userMapper = userMapper;
    }

    @Override
    public RoleEnum checkRoot(Long id) {
        User user = userRepository.getReferenceById(id);
        return user.getRoleEnum();
    }

    @Override
    public UserDTO getUser(String login) {
        Optional<User> u = userRepository.findUserByLogin(login);
        if (u.isPresent()) {
            User user = u.get();
            String group = null;
            String faculty = null;
            if (user.getRoleEnum() == RoleEnum.STUDENT) {
                if (user.getIdGroup() != null) {
                    group = groupService.getGroup(user.getIdGroup()).getNumber();
                }
                if (user.getIdFaculty() != null) {
                    faculty = facultyService.getOneFaculty(user.getIdFaculty()).getName();
                }
            }
            String role = null;
            RoleEnum R = user.getRoleEnum();
            switch (R) {
                case STUDENT -> role = "Студент";
                case EMPLOYEE -> role = "Сотрудник";
            }
            return userMapper.userToUserDTO(user, faculty, group, role);
        } else {
            throw new NotFoundUserException("No user in database on getUser method (UserServiceImpl.java");
        }
    }

    @Override
    public User getUserFrom(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new LocalNotFoundUserException("No user in database on getUserMethod");
        }
    }

    @Override
    public Long createUser(UserCreateDTO user) {
        if (userRepository.existsByLogin(user.login())){
            throw new RuntimeException("This user already exists");
        }
        RoleEnum r = RoleEnum.valueOf(user.role());
        User u = userMapper.userCreateDTOtoUser(user, r);
        User userCreated = userRepository.save(u);
        return userCreated.getId();
    }
    @Override
    public Set<Turn> getUserTurns(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            Set<Member> members = user.get().getMemberTurns();
            Iterator<Member> iterator = members.iterator();
            Set<Turn> turns = new HashSet<>();
            while(iterator.hasNext()){
                turns.add(iterator.next().getTurn());
            }
            return turns;
        } else {
            throw new LocalNotFoundUserException("No user on getUserTurns method (UserServiceImpl.java");
        }
    }

    @Override
    public void updateUser(User user) {
        userRepository.save(user);
    }

    @Override
    public User findByLogin(String login) {
        Optional<User> u = userRepository.findUserByLogin(login);
        if (u.isPresent()){
            return u.get();
        }
        else throw new NotFoundUserException("Auth error on findByLogin method (UserServiceImpl.java");
    }

    @Override
    public Long loginUser(String username, String password) {
        Optional<User> u = userRepository.findUserByLogin(username);
        if (u.isPresent()){
            if (u.get().getPassword().equals(password)){
               return u.get().getId();
            }
            else throw new AuthPasswordException("Auth error password");
        }
        else throw new NotFoundUserException("Auth error on loginUser method (UserServiceImpl.java");
    }


    @Override
    public UserDetailsService userDetailsService() {
        return this::findByLogin;
    }

    @Override
    public User getCurrentUser() {
        // Получение имени пользователя из контекста Spring Security
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return findByLogin(username);
    }
//    @Transactional
//    @Override
//    public void addTurn(Long userId, Long turnId) {
//        Turn t = turnService.getTurnFrom(turnId);
//        turnService.countUser(t);
//        Optional<User> user = userRepository.findById(userId);
//        if (user.isPresent()){
//            User u = user.get();
//            Set<Turn> turns = u.getTurns();
//            turns.add(t);
//            u.setTurns(turns);
//            userRepository.save(u);
//        }
//        else{
//            throw new NotFoundException("User not found");
//        }
//
//    }
}
