package com.eturn.eturn.repo;

import com.eturn.eturn.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface MmbRepo extends JpaRepository<Member,Long> {

    List<Member> findByIdUser(Long id_user);
    List<Member> findByIdTurnAndRootNot(Long id_turn, int root);
    List<Member> findByIdTurnAndRoot(Long id_turn, int root);

    void deleteByIdTurn(Long idTurn);
    void deleteByIdUser(Long idUser);






    Member getByIdUser(Long idUser);
    Member getByIdUserAndIdTurn(Long id_user, Long id_turn);

    boolean existsByIdUserAndIdTurn(Long id_user, Long id_group);

}
