package com.iasc;
import jakarta.persistence.*; import lombok.*; import java.time.Instant; import java.util.*;
@Entity @Table(name="users") @Getter @Setter
public class User {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(unique=true,nullable=false) private String email;
 private String fullName; private String passwordHash;
 @Enumerated(EnumType.STRING) private Role role=Role.USER;
 private Instant createdAt=Instant.now(), lastSeen, lastLogin;
 public enum Role{USER,ADMIN}
 public Map<String,Object> dto(){ return Map.of("id",id,"email",email,"fullName",fullName==null?"":fullName,"role",role.name()); }
}
