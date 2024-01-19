package com.assessment.demo.entity;

import com.assessment.demo.entity.base.BaseEntity;
import com.assessment.demo.service.RoleService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Slf4j
@Table(name = "Role")
public class Role extends BaseEntity {

    private static Map<Integer, String> ROLE_MAP;

    // This attribute is set up on the application.yml, so no need to auto increment
    @Id
    @Column(name = "roleId")
    private int roleId;

    @Column(name = "roleName", nullable = false, unique = true)
    private String roleName;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<User> users;

    // Default constructor, for creating roleConfig bean
    public Role() {
        super();
    }

    // Constructor for common use
    public Role(int roleId) {
        this();
        this.roleId = roleId;
        this.roleName = detectRole(roleId);
    }

    // Call this constructor when need to access the role table
    public Role(RoleService roleService) {
        ROLE_MAP = roleService.getRoleMap();
    }

    // And this constructor is for creating a new role in the database,
    //  used by ADMIN role or when program starts only!
    public Role(int roleId, String roleName) {
        this();
        this.roleId = roleId;
        this.roleName = roleName;
    }

    private String detectRole(int roleId) {
        // This map should be non-null due to postConstructor in RoleConfig
        try {
            String roleName = ROLE_MAP.get(roleId);
        }
        catch (Exception e){
            log.error("Please update the ROLE_MAP by call Role(roleService)");
        }

        if (roleName != null) {
            return roleName;
        } else {
            throw new RuntimeException("Invalid Role ID for generating!\n");
        }
    }
}
