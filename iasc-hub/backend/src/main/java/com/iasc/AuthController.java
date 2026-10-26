package com.iasc;
import jakarta.servlet.http.HttpServletResponse; import lombok.RequiredArgsConstructor; import org.springframework.beans.factory.annotation.Value; import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException; import java.io.IOException; import java.time.Instant; import java.util.*;
@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthController {
 private final UserRepo users; private final ResetTokenRepo tokens; private final PasswordEncoder enc; private final JwtService jwt; private final MailService mail;
 @Value("${app.front-url}") String front;
 static ResponseStatusException bad(String m){ return new ResponseStatusException(HttpStatus.BAD_REQUEST,m); }
 void sendError(HttpServletResponse rs, int status, String message) throws IOException {
  rs.setStatus(status); rs.setContentType("application/json"); rs.setCharacterEncoding("UTF-8");
  rs.getWriter().write("{\"message\":\""+message.replace("\"","'")+'"'+"}");
 }
 Map<String,Object> out(User u){ return Map.of("token",jwt.make(u),"user",u.dto()); }
 @PostMapping("/register") void register(@RequestBody Map<String,String> b, HttpServletResponse rs) throws IOException {
  String email=b.getOrDefault("email","").trim().toLowerCase(), pw=b.getOrDefault("password","");
  if(!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")){ sendError(rs,400,"Email invalide"); return; }
  if(pw.length()<6){ sendError(rs,400,"Mot de passe: 6 caractères minimum"); return; }
  if(users.existsByEmail(email)){ sendError(rs,400,"Cet email est déjà utilisé"); return; }
  var u=new User(); u.setEmail(email); u.setFullName(b.getOrDefault("fullName","")); u.setPasswordHash(enc.encode(pw)); u.setLastLogin(Instant.now()); u.setLastSeen(Instant.now()); users.save(u);
  mail.send(email,"Bienvenue sur IASC Hub","Bonjour "+u.getFullName()+",\n\nVotre compte a été créé avec succès. Bon apprentissage !\n\nIASC Hub");
  rs.setContentType("application/json"); rs.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(out(u)));
 }
 @PostMapping("/login") void login(@RequestBody Map<String,String> b, HttpServletResponse rs) throws IOException {
  var opt=users.findByEmail(b.getOrDefault("email","").trim().toLowerCase());
  if(opt.isEmpty()||!enc.matches(b.getOrDefault("password",""),opt.get().getPasswordHash())){ sendError(rs,400,"Email ou mot de passe incorrect"); return; }
  var u=opt.get(); u.setLastLogin(Instant.now()); u.setLastSeen(Instant.now()); users.save(u);
  rs.setContentType("application/json"); rs.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(out(u)));
 }
 @PostMapping("/forgot") Map<String,String> forgot(@RequestBody Map<String,String> b){
  users.findByEmail(b.getOrDefault("email","").trim().toLowerCase()).ifPresent(u->{
   var t=new ResetToken(); t.setToken(UUID.randomUUID().toString()); t.setUserId(u.getId()); t.setExpiresAt(Instant.now().plusSeconds(3600)); tokens.save(t);
   mail.send(u.getEmail(),"Réinitialisation du mot de passe","Cliquez pour choisir un nouveau mot de passe (valable 1h):\n"+front+"/reset?token="+t.getToken());
  }); return Map.of("ok","true");
 }
 @PostMapping("/reset") void reset(@RequestBody Map<String,String> b, HttpServletResponse rs) throws IOException {
  var t=tokens.findByToken(b.getOrDefault("token","")).filter(x->x.getExpiresAt().isAfter(Instant.now())).orElse(null);
  if(t==null){ sendError(rs,400,"Lien invalide ou expiré"); return; }
  String pw=b.getOrDefault("password",""); if(pw.length()<6){ sendError(rs,400,"Mot de passe: 6 caractères minimum"); return; }
  var u=users.findById(t.getUserId()).orElse(null); if(u==null){ sendError(rs,400,"Utilisateur introuvable"); return; }
  u.setPasswordHash(enc.encode(pw)); users.save(u); tokens.delete(t);
  mail.send(u.getEmail(),"Mot de passe modifié","Votre mot de passe vient d'être modifié.");
  rs.setContentType("application/json"); rs.getWriter().write("{\"ok\":\"true\"}");
 }
}
