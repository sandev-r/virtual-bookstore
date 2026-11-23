package com.sandev.virtualbookstoreapp.role.entity;

import com.sandev.virtualbookstoreapp.common.BaseEntity;
import com.sandev.virtualbookstoreapp.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@SuperBuilder
@Table(name = "_roles")
@EntityListeners(AuditingEntityListener.class)
public class Role extends BaseEntity {
    @Column(name = "ROLE_NAME")
    private String roleName;
    @ManyToMany(mappedBy = "roleList")
    private List<User> userList;
}
