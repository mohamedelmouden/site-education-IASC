package com.iasc;
import jakarta.persistence.*; import lombok.*;
@Entity @Getter @Setter
public class Subject { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; private String name; }
