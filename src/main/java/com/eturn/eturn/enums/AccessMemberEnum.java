package com.eturn.eturn.enums;

public enum AccessMemberEnum {
    BLOCKED,
    MEMBER,
    MODERATOR,
    CREATOR,
    MEMBER_LINK, // участник, который вступил по ссылке

    INVITED, // приглашенный модератор
    REFUSED; // отказавшийся модератор
    AccessMemberEnum() {
    }
}
