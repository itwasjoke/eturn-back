package com.eturn.eturn.repository;

import com.eturn.eturn.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group,Long> {

    boolean existsById(Long idGroup);
    Group getGroupById(Long id);
    Optional<Group> getGroupByNumber(String number);

}
