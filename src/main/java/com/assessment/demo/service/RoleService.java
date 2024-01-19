package com.assessment.demo.service;

import com.assessment.demo.entity.Role;
import com.assessment.demo.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface RoleService {
    public List<Role> getAllRoles();

    public Map<Integer, String> getRoleMap();

    public void createRoleIfNotExists(int roleId, String roleName);

    public Role getRoleById(int roleId);
}
