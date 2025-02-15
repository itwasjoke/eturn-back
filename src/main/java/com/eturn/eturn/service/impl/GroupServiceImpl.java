package com.eturn.eturn.service.impl;

import com.eturn.eturn.entity.Faculty;
import com.eturn.eturn.entity.Group;
import com.eturn.eturn.repository.GroupRepository;
import com.eturn.eturn.service.GroupService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Работа с учебными группами
 */
@Service
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    public GroupServiceImpl(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    /**
     * Получение опциональной группы
     * @param number текстовый номер группы
     * @return Потенциальная группа
     */
    @Override
    public Optional<Group> getGroup(String number) {
        return groupRepository.getGroupByNumber(number);
    }

    /**
     * Создание группы при регистрации пользователя,
     * у которого данные о группе могут не совпадать с БД
     * @param id индекс из профиля пользователя
     * @param number текстовый номер
     * @param course курс группы студента
     * @param faculty факультет группы студента
     */
    @Override
    public void createOptionalGroup(
            Long id,
            String number,
            Integer course,
            Faculty faculty
    ) {
        Optional<Group> group =
                groupRepository.getGroupByNumber(number);
        // Если такая группа уже существует, то мы просто обновляем ее информацию
        if (group.isPresent()){
            Group groupExisted = group.get();
            if (
                !Objects.equals(groupExisted.getCourse(), course)
                || !Objects.equals(
                        groupExisted.getFaculty().getId(),
                        faculty.getId()
                )
            ) {
                groupExisted.setFaculty(faculty);
                groupExisted.setCourse(course);
                groupRepository.save(groupExisted);
            }
        } else {
            // иначе создаем новую группу
            Group newGroup = new Group();
            newGroup.setFaculty(faculty);
            newGroup.setNumber(number);
            newGroup.setCourse(course);
            groupRepository.save(newGroup);
        }
    }
}
