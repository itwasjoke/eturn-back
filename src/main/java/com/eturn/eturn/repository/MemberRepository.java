package com.eturn.eturn.repository;

import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.AccessMemberEnum;
import com.eturn.eturn.enums.AccessTurnEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member,Long> {
    void deleteByTurn(Turn turn);
    Member getByUserAndTurn(User user, Turn turn);
    Optional<Member> findMemberByUserAndTurn(User user, Turn turn);
    Page<Member> getMemberByTurnAndAccessMemberEnum(Turn turn, AccessMemberEnum accessMemberEnum, Pageable pageable);
    List<Member> getMemberByTurnAndInvited(Turn turn, boolean invited);
    List<Member> getMemberByTurnAndAccessMemberEnumAndInvitedForTurn(Turn turn, AccessMemberEnum accessMemberEnum, boolean invitedForTurn);
    long countByTurnAndAccessMemberEnum(Turn turn, AccessMemberEnum accessMemberEnum);

    void deleteByTurnAndUser(Turn turn, User user);
    void deleteById(long id);
    @Modifying
    @Query("DELETE FROM Member m WHERE SIZE(m.positionsMember) = 0 AND m.turn = :turn AND m.accessMemberEnum = 'MEMBER'")
    void deleteMembersWithoutPositions(@Param("turn") Turn turn);
}
