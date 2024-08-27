package com.eturn.eturn.entity;

import com.eturn.eturn.enums.ApplicationType;
import com.eturn.eturn.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    private Long id;
    private String login;
    private String password;
    private String name;
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;
    @Enumerated(EnumType.STRING)
    private Role role;
    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
    private Set<Turn> createdTurns;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Member> memberTurns;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "turn_id")
    private Set<Position> positions;

    private String tokenNotification;

    @Enumerated(EnumType.STRING)
    private ApplicationType applicationType;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.toString()));
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
