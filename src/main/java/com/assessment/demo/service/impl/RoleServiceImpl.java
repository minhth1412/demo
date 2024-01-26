package com.assessment.demo.service.impl;

import com.assessment.demo.entity.Role;
import com.assessment.demo.repository.RoleRepository;
import com.assessment.demo.service.RoleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Map<Integer, String> getRoleMap() {
        List<Role> roles = getAllRoles();

        Map<Integer, String> roleMap = new HashMap<>();
        for (Role role : roles) {
            roleMap.put(role.getRoleId(), role.getRoleName());
        }
        return roleMap;
    }

    public void createRoleIfNotExists(int roleId, String roleName) {
        if (!roleRepository.existsById(roleId)) {
            roleRepository.save(new Role(roleId, roleName));
        }
    }

    public Role getRoleById(int roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with ID: " + roleId));
    }
}
