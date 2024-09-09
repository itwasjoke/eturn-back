package com.eturn.eturn.dto;

import java.util.List;

public record MemberListDTO(List<MemberDTO> list, long count) {
}
