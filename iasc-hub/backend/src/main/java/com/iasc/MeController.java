package com.iasc;
import lombok.RequiredArgsConstructor; import org.springframework.http.HttpStatus; import org.springframework.security.core.annotation.AuthenticationPrincipal; import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException; import java.util.*; import java.util.stream.*;
@RestController @RequestMapping("/api/me") @RequiredArgsConstructor
public class MeController {
 private final VisitRepo visits; private final MaterialRepo materials; private final UserRepo users; private final PasswordEncoder enc; private final MailService mail;
 @GetMapping Map<String,Object> me(@AuthenticationPrincipal User u){ return u.dto(); }
 @PostMapping("/ping") Map<String,String> ping(){ return Map.of("ok","true"); }
 @GetMapping("/stats") Map<String,Object> stats(@AuthenticationPrincipal User u){
  var vs=visits.findByUserIdOrderByAtDesc(u.getId()); Map<Long,Material> ms=new HashMap<>(); materials.findAll().forEach(m->ms.put(m.getId(),m));
  var by=vs.stream().filter(v->ms.containsKey(v.getMaterialId())).collect(Collectors.groupingBy(v->ms.get(v.getMaterialId()).getSubject().getName(),Collectors.counting()));
  var recent=vs.stream().filter(v->ms.containsKey(v.getMaterialId())).limit(8).map(v->{var m=ms.get(v.getMaterialId()); return Map.of("id",m.getId(),"title",m.getTitle(),"subject",m.getSubject().getName(),"at",v.getAt().toString());}).toList();
  return Map.of("totalVisits",vs.size(),"distinctLessons",vs.stream().map(Visit::getMaterialId).distinct().count(),"bySubject",by,"recent",recent);
 }
 @PostMapping("/password") Map<String,String> pw(@AuthenticationPrincipal User u,@RequestBody Map<String,String> b){
  if(!enc.matches(b.getOrDefault("current",""),u.getPasswordHash())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Mot de passe actuel incorrect");
  if(b.getOrDefault("password","").length()<6) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"6 caractères minimum");
  u.setPasswordHash(enc.encode(b.get("password"))); users.save(u); mail.send(u.getEmail(),"Mot de passe modifié","Votre mot de passe vient d'être modifié."); return Map.of("ok","true");
 }
}
