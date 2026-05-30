package org.social.common.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

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
    @JoinTable(
            name = "user_group",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private Set<Group> groups = new LinkedHashSet<>();

    @Size(max = 255)
    @Column(name = "password")
    private String password;

    @Column(name = "phone_number")
    private Integer phoneNumber;

    @Size(max = 255)
    @Column(name = "email")
    private String email;

    @Column(name = "is_active")
    private Boolean isActive;

    @Size(max = 255)
    @Column(name = "active_code")
    private String activeCode;

    @Column(name = "expire_date")
    private LocalDateTime expireDate;

    @OneToMany(mappedBy = "user")
    private Set<Comment> comments = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "conversation_user",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "conversation_id")
    )
    private Set<Conversation> conversations = new LinkedHashSet<>();

    @OneToMany(mappedBy = "sender")
    private Set<Message> messages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Post> posts = new LinkedHashSet<>();

    @Column(name = "active")
    private Boolean active;

    @Size(max = 500)
    @Column(name = "cover_photo", length = 500)
    private String coverPhoto;

    @Size(max = 101)
    @Column(name = "bio", length = 101)
    private String bio;

    @Size(max = 100)
    @Column(name = "location", length = 100)
    private String location;

    @Size(max = 200)
    @Column(name = "education", length = 200)
    private String education;

    @Size(max = 200)
    @Column(name = "workplace", length = 200)
    private String workplace;

    @Size(max = 100)
    @Column(name = "hometown", length = 100)
    private String hometown;

    @Size(max = 50)
    @Column(name = "relationship", length = 50)
    private String relationship;

    @Size(max = 20)
    @Column(name = "gender", length = 20)
    private String gender;

    @Size(max = 50)
    @Column(name = "pronouns", length = 50)
    private String pronouns;

    @Size(max = 50)
    @Column(name = "language", length = 50)
    private String language;
}
