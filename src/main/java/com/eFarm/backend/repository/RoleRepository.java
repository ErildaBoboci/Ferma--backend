package com.eFarm.backend.repository;

import com.eFarm.backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT r FROM Role r WHERE r.name = 'ADMIN'")
    Optional<Role> findAdminRole();

    @Query("SELECT r FROM Role r WHERE r.name = 'KUJDESTAR_KAFSHESH'")
    Optional<Role> findKujdestarKafsheshRole();

    @Query("SELECT r FROM Role r WHERE r.name = 'KUJDESTAR_FERME'")
    Optional<Role> findKujdestarFermeRole();
}