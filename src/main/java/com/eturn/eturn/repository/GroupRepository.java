package com.eturn.eturn.repository;

import com.eturn.eturn.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface GroupRepository extends JpaRepository<Group,Long> {
    boolean existsByNumber(String numGroup);
    boolean existsById(Long idGroup);
    Set<Group> findAllByFacultyId(Long id);
    //boolean existsById(int id_group);
    Group getByNumber(String numGroup);

}
