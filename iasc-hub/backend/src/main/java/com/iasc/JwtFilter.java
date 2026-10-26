package com.iasc;
import jakarta.servlet.*; import jakarta.servlet.http.*; import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder; import org.springframework.stereotype.Component; import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException; import java.time.Instant; import java.util.List;
@Component @RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
 private final JwtService jwt; private final UserRepo users;
 @Override protected void doFilterInternal(HttpServletRequest rq, HttpServletResponse rs, FilterChain ch) throws ServletException, IOException {
  String h=rq.getHeader("Authorization");
  if(h!=null&&h.startsWith("Bearer ")){ try{
   users.findByEmail(jwt.parse(h.substring(7))).ifPresent(u->{
    if(u.getLastSeen()==null||u.getLastSeen().isBefore(Instant.now().minusSeconds(30))){u.setLastSeen(Instant.now());users.save(u);}
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(u,null,List.of(new SimpleGrantedAuthority("ROLE_"+u.getRole()))));
   }); }catch(Exception ignored){} }
  ch.doFilter(rq,rs);
 }
}
