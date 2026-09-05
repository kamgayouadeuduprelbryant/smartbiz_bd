package com.smartbiz.security;

import com.smartbiz.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        var utilisateur = utilisateurRepository.findByEmailAvecRolesEtPermissions(email)                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Aucun compte trouve pour : " + email
                        ));

        return new UserPrincipal(utilisateur);
    }
}