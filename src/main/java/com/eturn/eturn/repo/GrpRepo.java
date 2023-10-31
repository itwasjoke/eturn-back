package com.eturn.eturn.repo;

import com.eturn.eturn.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrpRepo extends JpaRepository<Group,Long> {
    boolean existsByNumber(int numGroup);
    boolean existsById(int id_group);
    Group getByNumber(int numGroup);

}
