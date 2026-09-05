package com.smartbiz.security;

import com.smartbiz.model.Role;
import com.smartbiz.model.RoleType;
import com.smartbiz.model.Utilisateur;
import com.smartbiz.repository.RoleRepository;
import com.smartbiz.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Cree ou met a jour un Utilisateur local lors d'une connexion "Continuer avec Google".
 * Le compte est automatiquement rattache au role EMPLOYE par defaut ;
 * un administrateur peut ensuite lui attribuer un role plus eleve.
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String prenom = oAuth2User.getAttribute("given_name");
        String nom = oAuth2User.getAttribute("family_name");
        String photoUrl = oAuth2User.getAttribute("picture");

        if (email == null) {
            throw new OAuth2AuthenticationException("Google n'a pas fourni d'adresse email.");
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email).orElseGet(() -> {
            Role roleEmploye = roleRepository.findByNom(RoleType.EMPLOYE)
                    .orElseThrow(() -> new IllegalStateException("Le role EMPLOYE n'existe pas en base."));

            Utilisateur nouveau = Utilisateur.builder()
                    .email(email)
                    .nom(nom != null ? nom : "")
                    .prenom(prenom != null ? prenom : "")
                    .photoUrl(photoUrl)
                    .compteGoogle(true)
                    .actif(true)
                    .roles(Set.of(roleEmploye))
                    .build();
            return utilisateurRepository.save(nouveau);
        });

        return oAuth2User;
    }
}
