package com.assessment.demo.repository;

import com.assessment.demo.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Role findByRoleId(int roleId);
    boolean existsByRoleName(String role);
}
