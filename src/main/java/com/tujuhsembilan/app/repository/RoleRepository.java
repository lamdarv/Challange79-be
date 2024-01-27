package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    // Mencari role pertama berdasarkan nama rolenya
    Role findFirstByRoleName(String roleName);
}
