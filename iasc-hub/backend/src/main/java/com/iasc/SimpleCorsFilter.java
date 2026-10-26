package com.iasc;
import jakarta.servlet.*; import jakarta.servlet.http.*; import org.springframework.core.Ordered; import org.springframework.core.annotation.Order; import org.springframework.stereotype.Component;
import java.io.IOException;
@Component @Order(Ordered.HIGHEST_PRECEDENCE)
public class SimpleCorsFilter implements Filter {
 @Override public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
  HttpServletResponse r=(HttpServletResponse)res; HttpServletRequest q=(HttpServletRequest)req;
  r.setHeader("Access-Control-Allow-Origin","http://localhost:5173");
  r.setHeader("Access-Control-Allow-Methods","GET,POST,PUT,DELETE,OPTIONS");
  r.setHeader("Access-Control-Allow-Headers","*");
  r.setHeader("Access-Control-Expose-Headers","Content-Disposition");
  if("OPTIONS".equalsIgnoreCase(q.getMethod())){r.setStatus(HttpServletResponse.SC_OK);return;}
  chain.doFilter(req,res);
 }
}
