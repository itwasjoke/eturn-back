package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.TurnDTO;
import com.eturn.eturn.dto.UserCreateDTO;
import com.eturn.eturn.dto.UserDTO;
import com.eturn.eturn.dto.mapper.TurnListMapper;
import com.eturn.eturn.dto.mapper.UserMapper;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.RoleEnum;
import com.eturn.eturn.exception.NotFoundException;
import com.eturn.eturn.repository.UserRepository;
import com.eturn.eturn.service.CourseService;
import com.eturn.eturn.service.DepartmentService;
import com.eturn.eturn.service.FacultyService;
import com.eturn.eturn.service.GroupService;
import com.eturn.eturn.service.UserService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.factory.PasswordEncoderFactories;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

//    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final FacultyService facultyService;
    private final CourseService courseService;
    private final GroupService groupService;
    private final UserMapper userMapper;
    private final TurnListMapper turnListMapper;
    private final DepartmentService departmentService;

    public UserServiceImpl(UserRepository userRepository, FacultyService facultyService, CourseService courseService,
                           GroupService groupService, UserMapper userMapper, TurnListMapper turnListMapper,
                           DepartmentService departmentService) {
        this.userRepository = userRepository;
        this.facultyService = facultyService;
        this.courseService = courseService;
        this.groupService = groupService;
        this.userMapper = userMapper;
        this.turnListMapper = turnListMapper;
        this.departmentService = departmentService;
    }

    @Override
    public RoleEnum checkRoot(Long id) {
        User user = userRepository.getReferenceById(id);
        return user.getRoleEnum();
    }

    @Override
    public UserDTO getUser(Long id) {
        Optional<User> u = userRepository.findById(id);
        if (u.isPresent()) {
            User user = u.get();
            String group = null;
            String course = null;
            String department = null;
            String faculty = null;
            if (user.getRoleEnum() == RoleEnum.STUDENT) {
                if (user.getIdGroup() != null) {
                    group = groupService.getGroup(user.getIdGroup()).getNumber();
                }
                if (user.getIdCourse() != null) {
                    course = courseService.getOneCourse(user.getIdCourse()).getNumber().toString();
                }
                if (user.getIdFaculty() != null) {
                    faculty = facultyService.getOneFaculty(user.getIdFaculty()).getName();
                }
            } else if (user.getRoleEnum() == RoleEnum.EMPLOYEE || user.getRoleEnum() == RoleEnum.PROFESSOR) {
                if (user.getIdDepartment() != null) {
                    department = departmentService.getById(user.getIdDepartment()).getName();
                }

            }
            String role = null;
            RoleEnum R = user.getRoleEnum();
            switch (R) {
                case STUDENT -> role = "Студент";
                case EMPLOYEE -> role = "Сотрудник";
                case PROFESSOR -> role = "Преподаватель";
                case NO_UNIVERSITY -> role = "Посетитель";
            }
            return userMapper.userToUserDTO(user, faculty, course, department, group, role);
        } else {
            throw new NotFoundException("Пользователя не существует");
        }
    }

    @Override
    public User getUserFrom(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new NotFoundException("Пользователя не существует");
        }
    }

    @Override
    public Long createUser(UserCreateDTO user) {
        RoleEnum r = RoleEnum.valueOf(user.role());
        User u = userMapper.userCreateDTOtoUser(user, r);
        User userCreated = userRepository.save(u);
        return userCreated.getId();
    }

    @Override
    public Set<Turn> getUserTurns(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return user.get().getTurns();
        } else {
            throw new NotFoundException("User not found");
        }
    }

    @Override
    public List<TurnDTO> getUserTurnsDTO(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return turnListMapper.map(user.get().getTurns().stream().toList());
        } else {
            throw new NotFoundException("User not found");
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
        else throw new NotFoundException("User not found");
    }

    @Override
    public Long loginUser(String username, String password) {
        Optional<User> u = userRepository.findUserByLogin(username);
        if (u.isPresent()){
            if (u.get().getPassword().equals(password)){
               return u.get().getId();
            }
            else return -1L;
        }
        else return 0L;
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
