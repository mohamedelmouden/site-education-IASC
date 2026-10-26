package com.iasc;
import org.apache.pdfbox.Loader; import org.apache.pdfbox.pdmodel.PDDocument; import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xslf.usermodel.*; import org.apache.poi.xwpf.usermodel.*; import org.springframework.stereotype.Component;
import java.nio.file.*; import java.util.*;
/** Convertit DOCX / PPTX / PDF / TXT en HTML structuré (titres, listes, tableaux, images). */
@Component
public class Extractor {
 public static String esc(String s){ return s==null?"":s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;"); }
 static String img(byte[] d,String type){ if(type==null||type.contains("emf")||type.contains("wmf")) return ""; return "<img src=\"data:"+type+";base64,"+Base64.getEncoder().encodeToString(d)+"\"/>"; }
 public String extract(Path p,String name) throws Exception {
  String n=name.toLowerCase();
  if(n.endsWith(".docx")) return docx(p); if(n.endsWith(".pptx")) return pptx(p); if(n.endsWith(".pdf")) return pdf(p);
  if(n.endsWith(".txt")||n.endsWith(".md")) return "<h2>"+esc(name)+"</h2><pre>"+esc(Files.readString(p))+"</pre>";
  return "<p>Aperçu non disponible pour ce format. Téléchargez le fichier original.</p>";
 }
 String runs(XWPFParagraph pg){
  StringBuilder sb=new StringBuilder();
  for(XWPFRun r: pg.getRuns()){
   String t=esc(r.text()); if(!t.isEmpty()){ if(r.isBold()) t="<b>"+t+"</b>"; if(r.isItalic()) t="<i>"+t+"</i>"; if(r.getVerticalAlignment()!=null&&r.getVerticalAlignment().toString().equals("SUPERSCRIPT")) t="<sup>"+t+"</sup>"; sb.append(t); }
   for(XWPFPicture pic: r.getEmbeddedPictures()) sb.append(img(pic.getPictureData().getData(),pic.getPictureData().getPackagePart().getContentType()));
  } return sb.toString();
 }
 String docx(Path p) throws Exception {
  try(var in=Files.newInputStream(p); var d=new XWPFDocument(in)){
   StringBuilder sb=new StringBuilder(); boolean list=false;
   for(IBodyElement e: d.getBodyElements()){
    if(e instanceof XWPFParagraph pg){
     String inner=runs(pg); if(inner.isBlank()) continue; boolean li=pg.getNumID()!=null;
     if(li&&!list){ sb.append("<ul>"); list=true; } if(!li&&list){ sb.append("</ul>"); list=false; }
     String st=pg.getStyle()==null?"":pg.getStyle();
     if(li) sb.append("<li>").append(inner).append("</li>");
     else if(st.matches("(?i)(heading|titre)\\s*[1-3]")){ int lv=Integer.parseInt(st.replaceAll("\\D","")); sb.append("<h").append(lv+1).append(">").append(inner).append("</h").append(lv+1).append(">"); }
     else if(st.equalsIgnoreCase("Title")||st.equalsIgnoreCase("Titre")) sb.append("<h2>").append(inner).append("</h2>");
     else sb.append("<p>").append(inner).append("</p>");
    } else if(e instanceof XWPFTable t){
     if(list){ sb.append("</ul>"); list=false; } sb.append("<div class=\"tbl\"><table>");
     boolean first=true; for(XWPFTableRow r: t.getRows()){ sb.append("<tr>"); for(XWPFTableCell c: r.getTableCells()) sb.append(first?"<th>":"<td>").append(esc(c.getText()).replace("\n","<br/>")).append(first?"</th>":"</td>"); sb.append("</tr>"); first=false; }
     sb.append("</table></div>");
    }
   }
   if(list) sb.append("</ul>"); return sb.toString();
  }
 }
 String pptx(Path p) throws Exception {
  try(var in=Files.newInputStream(p); var ppt=new XMLSlideShow(in)){
   StringBuilder sb=new StringBuilder(); int i=1;
   for(XSLFSlide s: ppt.getSlides()){
    String title=s.getTitle(); sb.append("<h2>").append(esc(title==null||title.isBlank()?"Diapositive "+i:title)).append("</h2>");
    for(XSLFShape sh: s.getShapes()){
     if(sh instanceof XSLFTable t){ sb.append("<div class=\"tbl\"><table>"); boolean f=true; for(XSLFTableRow r: t.getRows()){ sb.append("<tr>"); for(XSLFTableCell c: r.getCells()) sb.append(f?"<th>":"<td>").append(esc(c.getText())).append(f?"</th>":"</td>"); sb.append("</tr>"); f=false; } sb.append("</table></div>"); }
     else if(sh instanceof XSLFTextShape ts){ for(XSLFTextParagraph tp: ts.getTextParagraphs()){ String x=tp.getText().trim(); if(x.isEmpty()||x.equals(title)) continue; sb.append("<p>").append(tp.isBullet()?"• ":"").append(esc(x)).append("</p>"); } }
     else if(sh instanceof XSLFPictureShape ps) sb.append(img(ps.getPictureData().getData(),ps.getPictureData().getContentType()));
    }
    i++;
   } return sb.toString();
  }
 }
 String pdf(Path p) throws Exception {
  try(PDDocument d=Loader.loadPDF(p.toFile())){
   var st=new PDFTextStripper(); st.setSortByPosition(true);
   // Extract each page text
   List<List<String>> pages=new ArrayList<>();
   for(int i=1;i<=d.getNumberOfPages();i++){
    st.setStartPage(i);st.setEndPage(i);
    List<String> paras=new ArrayList<>();
    for(String raw:st.getText(d).split("\\n\\s*\\n")){String t=raw.trim().replaceAll("\\s*\\n\\s*"," ");if(!t.isEmpty())paras.add(t);}
    pages.add(paras);
   }
   // Try to detect real headings across all pages
   StringBuilder sb=new StringBuilder(); boolean hasH=false;
   record Sec(String title,List<String> paras){}
   List<Sec> secs=new ArrayList<>(); String curTitle=null; List<String> curParas=new ArrayList<>();
   for(List<String> page:pages){ for(String t:page){ if(isHeading(t)){ if(!curParas.isEmpty()||curTitle!=null){secs.add(new Sec(curTitle,new ArrayList<>(curParas)));curParas.clear();} curTitle=t;hasH=true; }else curParas.add(t); } }
   if(!curParas.isEmpty()||curTitle!=null)secs.add(new Sec(curTitle,curParas));
   if(hasH){
    for(Sec s:secs){
     if(s.title()!=null)sb.append("<h2>").append(esc(s.title())).append("</h2>");
     for(String t:s.paras()){boolean h3=t.matches("^(\\d+(\\.\\d+)+\\.?|[a-z]\\))\\s+.{3,70}$");sb.append(h3?"<h3>":"<p>").append(esc(t)).append(h3?"</h3>":"</p>");}
    }
   }else{
    // No headings found → group every 4 pages into a section
    int G=4,total=pages.size(),ns=(total+G-1)/G;
    for(int s=0;s<ns;s++){
     int from=s*G+1,to=Math.min((s+1)*G,total);
     sb.append("<h2>").append(from==to?"Page "+from:"Pages "+from+"–"+to).append("</h2>");
     for(int pi=s*G;pi<Math.min((s+1)*G,total);pi++)for(String t:pages.get(pi))sb.append("<p>").append(esc(t)).append("</p>");
    }
   }
   return sb.toString();
  }
 }
 boolean isHeading(String t){
  if(t.length()>100||t.length()<3)return false;
  if(t.matches("(?i)(chapitre|chapter|partie|part|module|section|thème|theme|introduction|conclusion)\\s+\\w.*"))return true;
  if(t.matches("^\\d+(\\.\\d+)*\\.?\\s+[A-ZÀ-ÜÉÈÊa-zà-ü].{2,70}$")&&t.length()<=80)return true;
  String let=t.replaceAll("[^A-Za-zÀ-ÿ]","");
  if(let.length()>4&&let.length()<60&&let.equals(let.toUpperCase())&&t.length()<=70)return true;
  return false;
 }

}
