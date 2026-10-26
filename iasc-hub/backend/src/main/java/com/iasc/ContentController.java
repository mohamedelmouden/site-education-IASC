package com.iasc;
import lombok.RequiredArgsConstructor; import org.springframework.core.io.*; import org.springframework.http.*; import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal; import org.springframework.web.bind.annotation.*; import java.nio.charset.StandardCharsets; import java.nio.file.Path; import java.util.*;
@RestController @RequestMapping("/api") @RequiredArgsConstructor
public class ContentController {
 private final SubjectRepo subjects; private final MaterialRepo materials; private final VisitRepo visits; @Value("${app.upload-dir}") String dir;
 @GetMapping("/subjects") List<Map<String,Object>> list(){
  return subjects.findAll().stream().map(s->Map.<String,Object>of("id",s.getId(),"name",s.getName(),"materials",
   materials.findBySubjectIdOrderByUploadedAtAsc(s.getId()).stream().map(m->Map.of("id",m.getId(),"title",m.getTitle(),"kind",m.getKind(),"originalName",m.getOriginalName())).toList())).toList();
 }
 @GetMapping("/materials/{id}") Map<String,Object> get(@PathVariable Long id,@AuthenticationPrincipal User u){
  var m=materials.findById(id).orElseThrow(); visits.save(new Visit(u.getId(),id));
  return Map.of("id",m.getId(),"title",m.getTitle(),"kind",m.getKind(),"subject",m.getSubject().getName(),"originalName",m.getOriginalName(),"html",m.getContentHtml()==null?"":m.getContentHtml());
 }
 @GetMapping("/materials/{id}/download") ResponseEntity<Resource> dl(@PathVariable Long id){
  var m=materials.findById(id).orElseThrow(); Resource r=new FileSystemResource(Path.of(dir,m.getStoredName()));
  return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment().filename(m.getOriginalName(),StandardCharsets.UTF_8).build().toString()).contentType(MediaType.APPLICATION_OCTET_STREAM).body(r);
 }
}
