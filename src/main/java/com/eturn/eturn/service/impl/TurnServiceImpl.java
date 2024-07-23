package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.*;
import com.eturn.eturn.dto.mapper.TurnForListMapper;
import com.eturn.eturn.dto.mapper.TurnMapper;
import com.eturn.eturn.dto.mapper.TurnCreatingMapper;
import com.eturn.eturn.entity.*;
import com.eturn.eturn.enums.AccessMemberEnum;
import com.eturn.eturn.enums.AccessTurnEnum;
import com.eturn.eturn.enums.RoleEnum;
import com.eturn.eturn.exception.member.NoAccessMemberException;
import com.eturn.eturn.exception.member.NotFoundMemberException;
import com.eturn.eturn.exception.turn.*;
import com.eturn.eturn.repository.TurnRepository;
import com.eturn.eturn.security.HashGenerator;
import com.eturn.eturn.service.FacultyService;
import com.eturn.eturn.service.GroupService;
import com.eturn.eturn.service.MemberService;
import com.eturn.eturn.service.TurnService;
import com.eturn.eturn.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TurnServiceImpl implements TurnService {
    private final TurnRepository turnRepository;
    private final UserService userService;
    private final MemberService memberService;

    final private TurnCreatingMapper turnCreatingMapper;
    final private TurnForListMapper turnForListMapper;


    public TurnServiceImpl(
            TurnRepository turnRepository,
            UserService userService,
            MemberService memberService,
            TurnCreatingMapper turnCreatingMapper,
            TurnForListMapper turnForListMapper
    ) {
        this.turnRepository = turnRepository;
        this.userService = userService;
        this.memberService = memberService;
        this.turnCreatingMapper = turnCreatingMapper;
        this.turnForListMapper = turnForListMapper;
    }
    @Override
    public Turn getTurnFrom(String hash) {
        Optional<Turn> turn = turnRepository.findTurnByHash(hash);
        if(turn.isPresent()){
            return turn.get();
        }
        else{
            throw new LocalNotFoundTurnException("No turn in database on getTurnFrom method (TurnServiceImpl.java)");
        }
    }
    @Transactional
    @Override
    public List<TurnForListDTO> getUserTurns(String login, Map<String, String> params) {
        User user = userService.findByLogin(login);
        String access = params.get("Access");
        List<Object[]> allTurns = new ArrayList<>();
        Date now = new Date();
        turnRepository.deleteByDateEndIsLessThan(now);
        if (Objects.equals(access, "memberOut")) {
            allTurns = turnRepository.resultsMemberOut(user.getId(), user.getGroup().getId(), user.getGroup().getFaculty().getId(), params.get("Type"));
        } else if (Objects.equals(access, "memberIn")){
            allTurns = turnRepository.resultsMemberIn(user.getId(), params.get("Type"));
        }
        if (allTurns.isEmpty()){
            throw new NotFoundAllTurnsException("No turn in database on getUserTurns method (TurnServiceImpl.java)");
        }
        List<TurnForListDTO> turnForList = new ArrayList<>();
        for ( Object[] obj : allTurns ) {
            turnForList.add ( turnForListMapper.turnToTurnForListDTO( (Turn) obj[0], (String) obj[1] ));
        }
        return turnForList;
    }

    @Override
    @Transactional
    public String createTurn(TurnCreatingDTO turnDTO, String login) {
        if (turnDTO.dateEnd().getTime() > turnDTO.dateStart().getTime()) {
            UserDTO userDTO = userService.getUser(login);
            User user = userService.getUserFrom(userDTO.id());
            Date now = new Date();
            long timeDiff = turnDTO.dateEnd().getTime() - turnDTO.dateStart().getTime();
            long year = 1000*60*60*24*365L;
            long month = 1000*60*60*24*31L;
            if (user.getRoleEnum() == RoleEnum.STUDENT && (timeDiff > 1000*60*60*24*3 || timeDiff < 0))
                throw new InvalidTimeToCreateTurnException("Invalid turn duration");
            if (user.getRoleEnum() == RoleEnum.EMPLOYEE && (timeDiff > year || timeDiff < 0))
                throw new InvalidTimeToCreateTurnException("Invalid turn duration");
            if (user.getRoleEnum() == RoleEnum.STUDENT && ((turnDTO.dateStart().getTime() - now.getTime() > month) || (turnDTO.dateStart().getTime() - now.getTime() < 0)))
                throw new InvalidLengthTurnException("The turn is too long (or short)");
            if (user.getRoleEnum() == RoleEnum.EMPLOYEE && ((turnDTO.dateStart().getTime() - now.getTime() > month * 3) || (turnDTO.dateStart().getTime() - now.getTime() < 0)))
                throw new InvalidLengthTurnException("The turn is too long (or short)");
            User userCreator = userService.getUserFrom(userDTO.id());
            Turn turn = turnCreatingMapper.turnMoreDTOToTurn(turnDTO, userCreator);
            StringBuilder allowedElements = new StringBuilder(" ");
            if (turn.getAccessTurnType() == AccessTurnEnum.FOR_ALLOWED_ELEMENTS) {
                if (turn.getAllowedGroups() != null) {
                    for (Group group : turn.getAllowedGroups()) {
                        allowedElements.append(group.getNumber()).append(" ");
                    }
                }
                if (turn.getAllowedFaculties() != null) {
                    for (Faculty faculty : turn.getAllowedFaculties()) {
                        allowedElements.append(faculty.getName()).append(" ");
                    }
                }
            }
            turn.setAccessTags(allowedElements.toString().trim());
            String tags = turn.getName() + " " + turn.getDescription() + allowedElements + userCreator.getName();
            turn.setTags(tags);
            turn.setCountUsers(0);
            Turn turnNew = turnRepository.save(turn);
            String hash = HashGenerator.generateUniqueCode();
            Random random = new Random();
            int count = 0;
            while (turnRepository.existsAllByHash(hash)) {
                hash = HashGenerator.generateUniqueCode();
                count++;
                if (count>50) {
                    break;
                }
            }
            if (count>50) {
                // TODO Создать нормальное исключение
                throw new InvalidDataTurnException("error");
            }
            turnNew.setHash(hash);
            Turn turnWithHash = turnRepository.save(turnNew);
            memberService.createMember(userCreator, turnWithHash, "CREATOR");
            return turnWithHash.getHash();
        } else
            throw new InvalidDataTurnException("The dateEnd cannot be earlier than the dateStart on createTurn method (TurnServiceImpl.java)");

    }

    @Override
    @Transactional
    public void deleteTurn(String username, String hash) {
        Optional<Turn> turn = turnRepository.findTurnByHash(hash);
        UserDTO userDTO = userService.getUser(username);
        User user = userService.getUserFrom(userDTO.id());
        if (turn.isEmpty()){
            throw new NotFoundTurnException("No turn in database on deleteTurn method (TurnServiceImpl.java)");
        }
        AccessMemberEnum access = memberService.getAccess(user, turn.get());
        if (access == AccessMemberEnum.CREATOR) {
            turnRepository.deleteTurnById(turn.get().getId());
        } else {
            throw new NoAccessDeleteTurnException("Only creator can delete turn information on deleteTurn method (TurnServiceImpl.java)");
        }
    }
    @Override
    public void saveTurn(Turn turn) {
        turnRepository.save(turn);
    }

    @Override
    public List<TurnForListDTO> getLinkedTurn(String hash, String username) {
        Optional<Turn> t = turnRepository.findTurnByHash(hash);
        if (t.isEmpty()){
            throw new NotFoundAllTurnsException("no found");
        }
        Turn turn = t.get();
        UserDTO userDTO = userService.getUser(username);
        User user = userService.getUserFrom(userDTO.id());
        Optional<Member> member = memberService.getOptionalMember(user, turn);
        String access;
        if (member.isPresent()){
            access = member.get().getAccessMemberEnum().toString();
        }
        else {
            access = null;
        }
        List<TurnForListDTO> turnList = new ArrayList<>();
        turnList.add(turnForListMapper.turnToTurnForListDTO(turn, access));

        return turnList;
    }

    @Override
    public List<MemberDTO> getMemberList(String username, String type, String hash) {
        UserDTO userDTO = userService.getUser(username);
        User user = userService.getUserFrom(userDTO.id());
        Optional<Turn> turn = turnRepository.findTurnByHash(hash);
        if (turn.isEmpty()){
            throw new NotFoundTurnException("turn not found in getMember function");
        }
        Optional<Member> member = memberService.getOptionalMember(user, turn.get());
        if (member.isPresent()){
            AccessMemberEnum access = member.get().getAccessMemberEnum();
            if (access == AccessMemberEnum.CREATOR || access == AccessMemberEnum.MODERATOR){
                return memberService.getMemberList(turn.get(), type);
            }
            else{
                throw new NoAccessMemberException("No access");
            }
        }
        else {
            throw new NotFoundMemberException("no member");
        }
    }

}
