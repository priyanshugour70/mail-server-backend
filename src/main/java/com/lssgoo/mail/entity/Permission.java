package com.lssgoo.mail.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "permissions")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Permission extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "resource")
    private String resource;

    @Column(name = "action")
    private String action;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private List<Role> roles = new ArrayList<>();
}

