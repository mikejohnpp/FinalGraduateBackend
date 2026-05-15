package org.social.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users", schema = "FinalGraduateDB")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 150)
    @NotNull
    @Column(name = "user_name", nullable = false, length = 150)
    private String userName;

    @Size(max = 255)
    @NotNull
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Size(max = 150)
    @Column(name = "nick_name", length = 150)
    private String nickName;

    @Size(max = 255)
    @Column(name = "avatar")
    private String avatar;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @ColumnDefault("0")
    @Column(name = "is_delete")
    private Boolean isDelete;

    @ManyToMany
    private Set<Group> groups = new LinkedHashSet<>();

    @Size(max = 255)
    @Column(name = "password")
    private String password;

    @Column(name = "phone_number")
    private Integer phoneNumber;

    @Size(max = 255)
    @Column(name = "email")
    private String email;

    @Column(name = "isActive")
    private Boolean isActive;

    @Size(max = 255)
    @Column(name = "active_code")
    private String activeCode;

    @Column(name = "expire_date")
    private LocalDateTime expireDate;

    @OneToMany(mappedBy = "user")
    private Set<Comment> comments = new LinkedHashSet<>();

    @ManyToMany
    private Set<Conversation> conversations = new LinkedHashSet<>();

    @OneToMany(mappedBy = "sender")
    private Set<Message> messages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Post> posts = new LinkedHashSet<>();

    @ManyToMany
    private Set<User> users = new LinkedHashSet<>();
    @Column(name = "active")
    private Boolean active;


}