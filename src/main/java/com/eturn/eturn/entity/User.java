package com.eturn.eturn.entity;

import com.eturn.eturn.enums.RoleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String login;

    private String password;

    private String name;

    private Long idGroup;

    private Long idFaculty;

    @Enumerated(EnumType.STRING)
    private RoleEnum roleEnum;

//    @ManyToMany(cascade = CascadeType.ALL)
//    @JoinTable(
//            name = "user_turn",
//            joinColumns = @JoinColumn(name = "user_id"),
//            inverseJoinColumns = @JoinColumn(name = "turn_id")
//    )
//    private Set<Turn> turns;
//
    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
    private Set<Turn> createdTurns;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Member> memberTurns;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "turn_id")
    private Set<Position> positions;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(roleEnum.toString()));
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
