package com.eturn.eturn.repository;

import com.eturn.eturn.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member,Long> {
    void deleteByIdTurn(Long idTurn);

    Member getByIdUserAndIdTurn(Long id_user, Long id_turn);
}
