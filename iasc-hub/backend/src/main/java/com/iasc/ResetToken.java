package com.iasc;
import jakarta.persistence.*; import lombok.*; import java.time.Instant;
@Entity @Getter @Setter
public class ResetToken { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; private String token; private Long userId; private Instant expiresAt; }
