package com.iasc;
import lombok.RequiredArgsConstructor; import org.springframework.beans.factory.annotation.Value; import org.springframework.http.HttpStatus; import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*; import org.springframework.web.multipart.MultipartFile; import org.springframework.web.server.ResponseStatusException;
import java.nio.file.*; import java.time.*; import java.util.*; import java.util.stream.*;
@RestController @RequestMapping("/api/admin") @RequiredArgsConstructor
public class AdminController {
 private final UserRepo users; private final VisitRepo visits; private final MaterialRepo materials; private final SubjectRepo subjects; private final ResetTokenRepo tokens;
 private final PasswordEncoder enc; private final MailService mail; private final Extractor extractor; @Value("${app.upload-dir}") String dir;
 @GetMapping("/users") List<Map<String,Object>> users(){
  Instant lim=Instant.now().minusSeconds(120);
  return users.findAll().stream().map(u->{ Map<String,Object> m=new HashMap<>(u.dto()); m.put("online",u.getLastSeen()!=null&&u.getLastSeen().isAfter(lim));
   m.put("lastSeen",u.getLastSeen()==null?null:u.getLastSeen().toString()); m.put("createdAt",u.getCreatedAt().toString()); m.put("lessons",visits.findByUserIdOrderByAtDesc(u.getId()).size()); return m; }).toList();
 }
 @DeleteMapping("/users/{id}") @Transactional Map<String,String> del(@PathVariable Long id,@AuthenticationPrincipal User me){
  if(me.getId().equals(id)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Impossible de supprimer votre propre compte");
  visits.deleteByUserId(id); tokens.deleteByUserId(id); users.deleteById(id); return Map.of("ok","true");
 }
 @PutMapping("/users/{id}/password") Map<String,String> pw(@PathVariable Long id,@RequestBody Map<String,String> b){
  String p=b.getOrDefault("password",""); if(p.length()<6) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"6 caractères minimum");
  var u=users.findById(id).orElseThrow(); u.setPasswordHash(enc.encode(p)); users.save(u);
  mail.send(u.getEmail(),"Votre mot de passe a été modifié","Bonjour,\n\nVotre mot de passe a été modifié par l'administrateur. Si vous ne l'attendiez pas, utilisez « Mot de passe oublié »."); return Map.of("ok","true");
 }
 @GetMapping("/stats") Map<String,Object> stats(){
  Instant lim=Instant.now().minusSeconds(120); var all=users.findAll(); var vs=visits.findAll(); Map<Long,Material> ms=new HashMap<>(); materials.findAll().forEach(m->ms.put(m.getId(),m));
  Map<String,Long> days=new TreeMap<>(); for(int i=6;i>=0;i--) days.put(LocalDate.now().minusDays(i).toString(),0L);
  vs.forEach(v->days.computeIfPresent(v.getAt().atZone(ZoneId.systemDefault()).toLocalDate().toString(),(k,c)->c+1));
  var top=vs.stream().filter(v->ms.containsKey(v.getMaterialId())).collect(Collectors.groupingBy(v->ms.get(v.getMaterialId()).getTitle(),Collectors.counting())).entrySet().stream().sorted((a,b)->Long.compare(b.getValue(),a.getValue())).limit(8).map(e->Map.of("title",e.getKey(),"count",e.getValue())).toList();
  return Map.of("users",all.size(),"online",all.stream().filter(u->u.getLastSeen()!=null&&u.getLastSeen().isAfter(lim)).count(),"visits",vs.size(),"materials",ms.size(),"perDay",days,"top",top);
 }
 @PostMapping(value="/materials",consumes="multipart/form-data") Map<String,Object> upload(@RequestParam Long subjectId,@RequestParam String kind,@RequestParam String title,@RequestParam MultipartFile file) throws Exception {
  Files.createDirectories(Path.of(dir)); String orig=Optional.ofNullable(file.getOriginalFilename()).orElse("fichier"); String ext=orig.contains(".")?orig.substring(orig.lastIndexOf('.')):"";
  String stored=UUID.randomUUID()+ext; Path p=Path.of(dir,stored); file.transferTo(p.toAbsolutePath());
  var m=new Material(); m.setSubject(subjects.findById(subjectId).orElseThrow()); m.setTitle(title); m.setKind(kind); m.setOriginalName(orig); m.setStoredName(stored);
  try{ m.setContentHtml(extractor.extract(p,orig)); }catch(Exception e){ m.setContentHtml("<p>Extraction impossible: "+Extractor.esc(String.valueOf(e.getMessage()))+". Téléchargez le fichier original.</p>"); }
  materials.save(m); return Map.of("id",m.getId());
 }
 @DeleteMapping("/materials/{id}") @Transactional Map<String,String> delMat(@PathVariable Long id) throws Exception {
  var m=materials.findById(id).orElseThrow(); visits.deleteByMaterialId(id); Files.deleteIfExists(Path.of(dir,m.getStoredName())); materials.delete(m); return Map.of("ok","true");
 }
}
