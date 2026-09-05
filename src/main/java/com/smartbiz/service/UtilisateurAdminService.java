package com.smartbiz.service;

import com.smartbiz.dto.UtilisateurAdminDto;
import com.smartbiz.exception.ResourceNotFoundException;
import com.smartbiz.model.Role;
import com.smartbiz.model.Utilisateur;
import com.smartbiz.repository.RoleRepository;
import com.smartbiz.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UtilisateurAdminService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<Utilisateur> listerTous() {
        return utilisateurRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Utilisateur trouverParId(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
    }

    @Transactional
    public Utilisateur modifier(Long id, UtilisateurAdminDto dto) {
        Utilisateur utilisateur = trouverParId(id);
        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setActif(dto.isActif());

        Set<Role> roles = dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()
                ? new HashSet<>(roleRepository.findAllById(dto.getRoleIds()))
                : new HashSet<>();
        utilisateur.setRoles(roles);

        auditService.enregistrer("MODIFICATION", "Utilisateur", id.toString(),
                "Modification des roles/statut de " + utilisateur.getEmail());
        return utilisateur;
    }

    @Transactional
    public void basculerActivation(Long id) {
        Utilisateur utilisateur = trouverParId(id);
        utilisateur.setActif(!utilisateur.isActif());
        auditService.enregistrer("MODIFICATION", "Utilisateur", id.toString(),
                (utilisateur.isActif() ? "Activation" : "Desactivation") + " du compte " + utilisateur.getEmail());
    }

    @Transactional(readOnly = true)
    public List<Role> listerRoles() {
        return roleRepository.findAll();
    }
}
