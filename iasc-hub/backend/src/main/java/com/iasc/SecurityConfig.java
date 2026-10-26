package com.iasc;
import org.springframework.context.annotation.*; import org.springframework.http.HttpMethod; import org.springframework.security.config.Customizer; import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy; import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain; import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*; import java.util.List;
@Configuration
public class SecurityConfig {
 @Bean SecurityFilterChain chain(HttpSecurity http, JwtFilter jwt) throws Exception {
  http.csrf(c->c.disable()).cors(Customizer.withDefaults()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .exceptionHandling(e->e.authenticationEntryPoint((q,r,x)->{
    r.setStatus(401); r.setContentType("application/json"); r.setCharacterEncoding("UTF-8");
    r.getWriter().write("{\"message\":\"Non authentifi\u00e9\"}");
   }))
   .authorizeHttpRequests(a->a
    .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .anyRequest().authenticated())
   .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class);
  return http.build();
 }
 @Bean PasswordEncoder encoder(){ return new BCryptPasswordEncoder(); }
 @Bean CorsConfigurationSource cors(){
  var c=new CorsConfiguration();
  c.setAllowedOrigins(List.of("http://localhost:5173"));
  c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
  c.setAllowedHeaders(List.of("*"));
  c.setExposedHeaders(List.of("Content-Disposition"));
  var s=new UrlBasedCorsConfigurationSource();
  s.registerCorsConfiguration("/**",c);
  return s;
 }
}
