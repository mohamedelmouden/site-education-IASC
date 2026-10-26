package com.iasc;
import lombok.RequiredArgsConstructor; import org.springframework.beans.factory.annotation.Value; import org.springframework.mail.SimpleMailMessage; import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async; import org.springframework.stereotype.Service;
@Service @RequiredArgsConstructor
public class MailService {
 private final JavaMailSender sender; @Value("${spring.mail.username:}") private String from;
 @Async public void send(String to,String subject,String body){
  if(from.isBlank()){ System.out.println("[MAIL non configuré] "+to+" | "+subject+"\n"+body); return; }
  try{ var m=new SimpleMailMessage(); m.setFrom(from); m.setTo(to); m.setSubject(subject); m.setText(body); sender.send(m); }catch(Exception e){ System.err.println("Erreur mail: "+e.getMessage()); }
 }
}
