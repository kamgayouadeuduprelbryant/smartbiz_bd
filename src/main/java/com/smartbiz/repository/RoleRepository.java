package com.smartbiz.repository;

import com.smartbiz.model.Role;
import com.smartbiz.model.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByNom(RoleType nom);
}
