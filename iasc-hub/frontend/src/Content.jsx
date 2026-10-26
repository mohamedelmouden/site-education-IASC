import {useEffect,useMemo,useRef,useState} from 'react'; import {Link,useNavigate,useParams} from 'react-router-dom'; import {api,download} from './api';
import renderMathInElement from 'katex/dist/contrib/auto-render'; import 'katex/dist/katex.min.css';
export function Courses(){
 const [subs,setSubs]=useState([]); useEffect(()=>{api.get('/subjects').then(r=>setSubs(r.data));},[]);
 return <><h1>Cours</h1><div className="grid3">{subs.map(x=><Link key={x.id} to={'/courses/'+x.id} className="card subj"><h4>{x.name}</h4><small>{x.materials.length} contenu(s)</small></Link>)}</div></>;
}
export function Subject(){
 const {id}=useParams(); const [s,setS]=useState(null); useEffect(()=>{api.get('/subjects').then(r=>setS(r.data.find(x=>x.id==id)));},[id]); if(!s) return null;
 const kinds=[...new Set(s.materials.map(m=>m.kind))];
 return <><Link to="/courses">← Cours</Link><h1>{s.name}</h1>{!kinds.length&&<p className="muted">Aucun contenu pour le moment.</p>}
  {kinds.map(k=><div key={k} className="card"><h3>{k}</h3>{s.materials.filter(m=>m.kind===k).map(m=><div key={m.id} className="row"><Link to={'/lesson/'+m.id}>📄 {m.title}</Link><button className="btn sm" onClick={()=>download('/materials/'+m.id+'/download',m.originalName)}>⬇ Original</button></div>)}</div>)}</>;
}
export function Lesson(){
 const {id}=useParams(); const nav=useNavigate(); const [m,setM]=useState(null); const [i,setI]=useState(0); const ref=useRef();
 useEffect(()=>{setM(null);api.get('/materials/'+id).then(r=>{setM(r.data);setI(+(localStorage.getItem('sec'+id)||0));});},[id]);
 const secs=useMemo(()=>{ if(!m) return []; const d=new DOMParser().parseFromString(m.html,'text/html'); const out=[]; let c={title:'Introduction',html:''};
  [...d.body.children].forEach(n=>{ if(n.tagName==='H2'){ if(c.html) out.push(c); c={title:n.textContent,html:n.outerHTML}; } else c.html+=n.outerHTML; }); if(c.html) out.push(c); return out; },[m]);
 useEffect(()=>{ if(ref.current) renderMathInElement(ref.current,{delimiters:[{left:'$$',right:'$$',display:true},{left:'$',right:'$',display:false}],throwOnError:false}); window.scrollTo(0,0); },[i,secs]);
 const go=k=>{setI(k);localStorage.setItem('sec'+id,k)};
 if(!m) return null; const cur=secs[i]; const pct=secs.length?Math.round((i+1)/secs.length*100):0;
 return <div className="lesson"><div className="lhead"><div><small>{m.subject} · {m.kind}</small><h2>{m.title}</h2></div>
   <button className="btn" onClick={()=>download('/materials/'+id+'/download',m.originalName)}>⬇ Télécharger l’original</button></div>
  <div className="meter big"><i style={{width:pct+'%'}}/></div>
  <div className="lbody"><nav className="outline"><h4>Plan du cours</h4>{secs.map((s,k)=><button key={k} className={k===i?'on':''} onClick={()=>go(k)}><span className={k<i?'dot done':'dot'}>{k<i?'✓':k+1}</span>{s.title}</button>)}</nav>
   <article className="card content"><div ref={ref} dangerouslySetInnerHTML={{__html:cur?.html||'<p>Contenu vide.</p>'}}/>
    <div className="pager"><button className="btn light" disabled={i===0} onClick={()=>go(i-1)}>← Précédent</button><span>{i+1} / {secs.length}</span>
     {i<secs.length-1?<button className="btn" onClick={()=>go(i+1)}>Suivant →</button>:<button className="btn" onClick={()=>nav(-1)}>Terminer ✔</button>}</div></article></div></div>;
}
