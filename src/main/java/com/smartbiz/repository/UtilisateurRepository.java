package com.smartbiz.repository;

import com.smartbiz.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * A utiliser pour l'authentification : charge en une seule requete
     * l'utilisateur, ses roles et les permissions de chaque role, afin
     * d'eviter tout LazyInitializationException une fois la session fermee
     * (Role.permissions est en FetchType.LAZY).
     */
    @Query("SELECT DISTINCT u FROM Utilisateur u " +
            "LEFT JOIN FETCH u.roles r " +
            "LEFT JOIN FETCH r.permissions " +
            "WHERE u.email = :email")
    Optional<Utilisateur> findByEmailAvecRolesEtPermissions(@Param("email") String email);
}