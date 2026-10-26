package com.iasc;
import lombok.RequiredArgsConstructor; import org.springframework.beans.factory.annotation.Value; import org.springframework.boot.CommandLineRunner; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor
public class DataInit implements CommandLineRunner {
 private final UserRepo users; private final SubjectRepo subjects; private final PasswordEncoder enc;
 @Value("${app.admin-email}") String email; @Value("${app.admin-password}") String pass;
 public void run(String... a){
  if(subjects.count()==0) for(String n: new String[]{"Math pour l'IA","Modèles et systèmes de données complexes","Français","Modélisation","Graphes et réseaux","Logique pour l'IA","Cybersécurité et Blockchain"}){ var s=new Subject(); s.setName(n); subjects.save(s); }
  if(!users.existsByEmail(email)){ var u=new User(); u.setEmail(email); u.setFullName("Administrateur"); u.setPasswordHash(enc.encode(pass)); u.setRole(User.Role.ADMIN); users.save(u); }
 }
}
