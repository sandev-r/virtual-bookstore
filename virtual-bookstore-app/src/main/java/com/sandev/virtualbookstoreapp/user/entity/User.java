package com.sandev.virtualbookstoreapp.user.entity;

import com.sandev.virtualbookstoreapp.role.entity.Role;
import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static com.sandev.virtualbookstoreapp.user.entity.AccountStatus.ACTIVE;
import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.GenerationType.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "_users")
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails{

    @Id
    @GeneratedValue(strategy = UUID)
    private String id;
    @Column(name = "FIRST_NAME", nullable = false)
    private String firstName;
    @Column(name = "LAST_NAME", nullable = false)
    private String lastName;
    @Column(name = "DATE_OF_BIRTH")
    private LocalDate dateOfBirth;
    @Column(name = "EMAIL",  unique = true,  nullable = false)
    private String email;
    @Column(name = "PHONE_NUMBER",  unique = true,  nullable = false)
    private String phoneNumber;
    @Column(name = "PASSWORD", nullable = false)
    private String password;
    @Column(name = "IS_ACCOUNT_ENABLED")
    private boolean isAccountEnabled;
    @Column(name = "IS_ACCOUNT_LOCKED")
    private boolean isAccountLocked;
    @Column(name = "IS_CREDENTIALS_EXPIRED")
    private boolean isCredentialsExpired;
    @Column(name = "IS_EMAIL_VERIFIED")
    private boolean isEmailVerified;
    @Column(name = "IS_PHONE_VERIFIED")
    private boolean isPhoneVerified;

    @CreatedDate
    @Column(name = "CREATED_AT", updatable = false, nullable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE", insertable = false)
    private LocalDateTime lastModifiedAt;

    @ManyToMany(fetch = EAGER, cascade = {PERSIST, MERGE})
    @JoinTable(
            name = "USER_ROLE",
            joinColumns = {
                    @JoinColumn(name = "USER_ID")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "ROLE_ID"),
            }
    )
    private List<Role> roleList;

    // This part is used to schedule the deletion
    @Enumerated(EnumType.STRING)
    @Column(name = "ACCOUNT_STATUS")
    @Builder.Default
    private AccountStatus accountStatus =  ACTIVE;

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        if(CollectionUtils.isEmpty(roleList)){
            return List.of();
        }
        return roleList.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                .toList();
    }

    @Override
    public @Nullable String getPassword() {
        return this.password;
    }

    @Override
    public @NonNull String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.isAccountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !this.isCredentialsExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.isAccountEnabled
                && this.accountStatus == ACTIVE
                && this.isEmailVerified;
    }
}
