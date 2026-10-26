package com.iasc;
import jakarta.persistence.*; import lombok.*; import java.time.Instant;
@Entity @Getter @Setter
public class Material {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.EAGER) private Subject subject;
 private String title, kind, originalName, storedName;
 @Lob @Column(columnDefinition="LONGTEXT") private String contentHtml;
 private Instant uploadedAt=Instant.now();
}
