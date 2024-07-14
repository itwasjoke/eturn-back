package com.eturn.eturn.repository;

import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.AccessMemberEnum;
import com.eturn.eturn.enums.AccessTurnEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member,Long> {
    void deleteByTurn(Turn turn);
    Member getByUserAndTurn(User user, Turn turn);
    Optional<Member> findMemberByUserAndTurn(User user, Turn turn);
    List<Member> getMemberByTurnAndAccessMemberEnum(Turn turn, AccessMemberEnum accessMemberEnum);
    long countByTurn(Turn turn);


    void deleteById(long id);
}
