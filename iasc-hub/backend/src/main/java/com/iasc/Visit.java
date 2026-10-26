package com.iasc;
import jakarta.persistence.*; import lombok.*; import java.time.Instant;
@Entity @Getter @Setter
public class Visit {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private Long userId, materialId; private Instant at=Instant.now();
 public Visit(){} public Visit(Long u,Long m){userId=u;materialId=m;}
}
