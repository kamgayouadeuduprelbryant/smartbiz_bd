package com.smartbiz.repository;

import com.smartbiz.model.Notification;
import com.smartbiz.model.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("select n from Notification n where n.destinataire = :utilisateur or n.destinataire is null " +
           "order by n.dateCreation desc")
    Page<Notification> trouverPourUtilisateur(@Param("utilisateur") Utilisateur utilisateur, Pageable pageable);

    @Query("select count(n) from Notification n where (n.destinataire = :utilisateur or n.destinataire is null) " +
           "and n.lue = false")
    long compterNonLuesPourUtilisateur(@Param("utilisateur") Utilisateur utilisateur);
}
