package com.eturn.eturn.additionalService.turn.impl.data;

import com.eturn.eturn.dto.MembersCountDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TurnDetails {
    private String access;
    private boolean invitedForModerator;
    private String invitedForTurn;
    private boolean existsInvited;
    private MembersCountDTO membersCountDTO;
    private long positionsCount;
}
