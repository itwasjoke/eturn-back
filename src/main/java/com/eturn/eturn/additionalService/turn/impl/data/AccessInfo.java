package com.eturn.eturn.additionalService.turn.impl.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AccessInfo {
    private String accessType;
    private List<Long> allowedIds;
}
