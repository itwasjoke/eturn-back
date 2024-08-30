package com.eturn.eturn.repository;

import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.AccessMember;
import com.eturn.eturn.enums.InvitedStatus;
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
    Optional<Member> findMemberByUserAndTurn(User user, Turn turn);
    Page<Member> getMemberByTurnAndAccessMember(Turn turn, AccessMember accessMember, Pageable pageable);
    List<Member> getMemberByTurnAndInvited(Turn turn, boolean invited);
    Page<Member> getMemberByTurnAndAccessMemberAndInvitedForTurn(Turn turn, AccessMember accessMember, InvitedStatus invitedStatus, Pageable pageable);
    int countByTurnAndInvited(Turn turn, boolean invited);
    int countByTurnAndInvitedForTurn(Turn turn, InvitedStatus invitedStatus);
    long countByTurnAndAccessMember(Turn turn, AccessMember accessMember);
    void deleteByTurnAndUser(Turn turn, User user);
    void deleteById(long id);
    @Query("SELECT 1 FROM Member m WHERE (m.invited = :i1 OR m.invitedForTurn = :i2) AND m.turn = :turn ")
    Optional<Member> getOneInvitedExists(@Param("turn") Turn turn, @Param("i1") boolean i1, @Param("i2") boolean i2);
    @Modifying
    @Query("DELETE FROM Member m WHERE SIZE(m.positionsMember) = 0 AND m.turn = :turn AND m.accessMember = 'MEMBER'")
    void deleteMembersWithoutPositions(@Param("turn") Turn turn);
    List<Member> getAllByTurn_IdAndAccessMember(long turnId, AccessMember accessMember);
    Member getMemberByTurn_IdAndAccessMember(long turnId, AccessMember accessMember);
}
