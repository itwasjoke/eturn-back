package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.UserCreateDTO;
import com.eturn.eturn.dto.UserDTO;
import com.eturn.eturn.dto.mapper.UserMapper;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.RoleEnum;
import com.eturn.eturn.exception.NotFoundException;
import com.eturn.eturn.repository.UserRepository;
import com.eturn.eturn.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FacultyService facultyService;

    private final TurnService turnService;
    private final CourseService courseService;
    private final GroupService groupService;
    private final UserMapper userMapper;
    private final DepartmentService departmentService;

    public UserServiceImpl(UserRepository userRepository, FacultyService facultyService, TurnService turnService, CourseService courseService, GroupService groupService, UserMapper userMapper, DepartmentService departmentService) {
        this.userRepository = userRepository;
        this.facultyService = facultyService;
        this.turnService = turnService;
        this.courseService = courseService;
        this.groupService = groupService;
        this.userMapper = userMapper;
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
        if (u.isPresent()){
            User user = u.get();
            String group = null;
            String course = null;
            String department = null;
            String faculty = null;
            if (user.getRoleEnum()==RoleEnum.STUDENT){
                if (user.getIdGroup()!=null){
                    group = groupService.getGroup(user.getIdGroup()).getNumber();
                }
                if (user.getIdCourse()!=null){
                    course = courseService.getOneCourse(user.getIdCourse()).getNumber().toString();
                }
                if (user.getIdFaculty()!=null){
                    faculty = facultyService.getOneFaculty(user.getIdFaculty()).getName();
                }
            }
            else if (user.getRoleEnum()==RoleEnum.EMPLOYEE || user.getRoleEnum() == RoleEnum.PROFESSOR){
                if (user.getIdDepartment()!=null){
                    department = departmentService.getById(user.getIdDepartment()).getName();
                }

            }
            String role = null;
            RoleEnum R = user.getRoleEnum();
            switch (R){
                case STUDENT -> role = "Студент";
                case EMPLOYEE -> role = "Сотрудник";
                case PROFESSOR -> role = "Преподаватель";
                case NO_UNIVERSITY -> role = "Посетитель";
            }
            return userMapper.userToUserDTO(user, faculty, course, department, group, role);
        }
        else{
            throw new NotFoundException("Пользователя не существует");
        }
//        if (userRepository.existsById(id)){
//            return userRepository.getReferenceById(id);
//        }
//        else{
//            throw new NotFoundException("Пользователя не существует");
//        }
    }

    @Override
    public User getUserFrom(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()){
            return user.get();
        }
        else{
            throw new NotFoundException("Пользователя не существует");
        }
    }

    @Override
    public Long createUser(UserCreateDTO user) {
        RoleEnum r = RoleEnum.valueOf(user.role());
        User userCreated = userRepository.save(userMapper.userCreateDTOtoUser(user, r));
        return userCreated.getId();
    }

    @Override
    public Set<Turn> getUserTurns(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()){
            return user.get().getTurns();
        }
        else{
            throw new NotFoundException("User not found");
        }
    }
    @Transactional
    @Override
    public void addTurn(Long userId, Long turnId) {
        Turn t = turnService.getTurnFrom(turnId);
        turnService.countUser(t);
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()){
            User u = user.get();
            Set<Turn> turns = u.getTurns();
            turns.add(t);
            u.setTurns(turns);
            userRepository.save(u);
        }
        else{
            throw new NotFoundException("User not found");
        }

    }
}
