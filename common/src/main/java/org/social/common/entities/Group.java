package org.social.common.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "`groups`", schema = "FinalGraduateDB")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 255)
    @Column(name = "cover_photo")
    private String coverPhoto;

    @Size(max = 255)
    @Column(name = "avatar")
    private String avatar;

    @Size(max = 20)
    @Column(name = "privacy", length = 20)
    private String privacy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin;

    @OneToMany(mappedBy = "group")
    private Set<Post> posts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "group")
    private Set<UserGroup> userGroups = new LinkedHashSet<>();

    @org.hibernate.annotations.ColumnDefault("1")
    @Column(name = "is_active")
    private Boolean isActive;


}
