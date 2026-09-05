package com.smartbiz.security;

import com.smartbiz.model.Role;
import com.smartbiz.model.Utilisateur;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {

    private final Utilisateur utilisateur;

    public UserPrincipal(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public Long getId() {
        return utilisateur.getId();
    }

    public String getNomComplet() {
        return utilisateur.getPrenom() + " " + utilisateur.getNom();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return utilisateur.getRoles().stream()
                .flatMap(role -> {
                    var roleAuthority =
                            new SimpleGrantedAuthority(
                                    "ROLE_" + role.getNom().name()
                            );

                    var permissionAuthorities =
                            role.getPermissions().stream()
                                    .map(p ->
                                            new SimpleGrantedAuthority(
                                                    p.getCode()
                                            )
                                    );

                    return java.util.stream.Stream.concat(
                            java.util.stream.Stream.of(roleAuthority),
                            permissionAuthorities
                    );
                })
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return utilisateur.getMotDePasse();
    }

    @Override
    public String getUsername() {
        return utilisateur.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return utilisateur.isActif();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return utilisateur.isActif();
    }
}