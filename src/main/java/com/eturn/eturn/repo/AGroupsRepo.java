package com.eturn.eturn.repo;

import com.eturn.eturn.domain.AllowGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface AGroupsRepo extends JpaRepository<AllowGroup,Long> {
    List<AllowGroup> findByIdGroup(Long idGroup);
    List<AllowGroup> findByIdTurn(Long idTurn);
    void deleteByIdTurn(Long idTurn);
    void deleteByIdGroup(Long idGroup);

    AllowGroup getByIdTurnAndIdGroup(Long id_turn, Long id_group);
}
