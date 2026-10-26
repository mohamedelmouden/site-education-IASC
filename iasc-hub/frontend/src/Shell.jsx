import {useContext,useEffect,useState} from 'react'; import {NavLink,Outlet,Link} from 'react-router-dom'; import {api,AuthCtx} from './api';
export function Shell(){
 const {user,logout}=useContext(AuthCtx);
 useEffect(()=>{const p=()=>api.post('/me/ping').catch(()=>{}); p(); const i=setInterval(p,60000); return ()=>clearInterval(i);},[]);
 const L=({to,children,end})=><NavLink to={to} end={end} className={({isActive})=>'nav'+(isActive?' on':'')}>{children}</NavLink>;
 return <div className="shell"><aside><div className="logo">IASC<b>Hub</b></div>
  <L to="/" end>▦ Dashboard</L><L to="/courses">📚 Cours</L><L to="/plan">🗓 Plan de révision</L><L to="/stats">📈 Statistiques</L>
  {user.role==='ADMIN'&&<><div className="sec">Administration</div><L to="/admin">⚙ Gestion du site</L></>}
  <div className="grow"/><L to="/profile"><span className="av">{(user.fullName||user.email)[0].toUpperCase()}</span> Profil</L><button className="nav out" onClick={logout}>⎋ Déconnexion</button></aside>
  <main><Outlet/></main></div>;
}
const Bars=({data})=>{const mx=Math.max(1,...Object.values(data));return <div className="bars">{Object.entries(data).map(([k,v])=><div key={k} className="bar"><i style={{height:(v/mx*100)+'%'}}/><span>{v}</span><small>{k.slice(-5)}</small></div>)}</div>};
export function Dashboard(){
 const {user}=useContext(AuthCtx); const [s,setS]=useState(null); const [subs,setSubs]=useState([]);
 useEffect(()=>{api.get('/me/stats').then(r=>setS(r.data));api.get('/subjects').then(r=>setSubs(r.data));},[]);
 const last=s?.recent?.[0];
 return <><h1>Bienvenue, {user.fullName||user.email} 👋</h1><p className="muted">Continuez votre apprentissage.</p>
  <div className="grid3"><div className="card hero"><h3>Continuez à apprendre chaque jour</h3>{last?<Link className="btn light" to={'/lesson/'+last.id}>Reprendre : {last.title}</Link>:<Link className="btn light" to="/courses">Explorer les cours</Link>}</div>
   <div className="card"><h4>Leçons consultées</h4><div className="big">{s?.distinctLessons??0}</div></div><div className="card"><h4>Ouvertures totales</h4><div className="big">{s?.totalVisits??0}</div></div></div>
  <h2>Mes matières</h2><div className="grid3">{subs.map(x=><Link key={x.id} to={'/courses/'+x.id} className="card subj"><h4>{x.name}</h4><small>{x.materials.length} contenu(s)</small></Link>)}</div></>;
}
export function Stats(){
 const [s,setS]=useState(null); useEffect(()=>{api.get('/me/stats').then(r=>setS(r.data));},[]); if(!s) return null;
 return <><h1>Mes statistiques</h1><div className="grid3"><div className="card"><h4>Leçons distinctes</h4><div className="big">{s.distinctLessons}</div></div><div className="card"><h4>Ouvertures</h4><div className="big">{s.totalVisits}</div></div></div>
  <div className="card"><h3>Par matière</h3>{Object.entries(s.bySubject).map(([k,v])=><div key={k} className="row"><span>{k}</span><div className="meter"><i style={{width:Math.min(100,v*10)+'%'}}/></div><b>{v}</b></div>)}{!Object.keys(s.bySubject).length&&<p className="muted">Aucune activité.</p>}</div>
  <div className="card"><h3>Récemment consultés</h3>{s.recent.map((r,i)=><div key={i} className="row"><Link to={'/lesson/'+r.id}>{r.title}</Link><small>{r.subject} · {new Date(r.at).toLocaleString('fr-FR')}</small></div>)}</div></>;
}
export function Plan(){
 const [d,setD]=useState(localStorage.getItem('exam')||''); const [h,setH]=useState(3); const [subs,setSubs]=useState([]);
 useEffect(()=>{api.get('/subjects').then(r=>setSubs(r.data));},[]);
 const days=d?Math.max(0,Math.ceil((new Date(d)-new Date())/864e5)):0; const slots=Math.max(1,Math.floor(h/1.5));
 const plan=subs.length?Array.from({length:Math.min(days,60)},(_,i)=>({date:new Date(Date.now()+i*864e5).toLocaleDateString('fr-FR',{weekday:'long',day:'numeric',month:'long'}),items:Array.from({length:slots},(_,k)=>subs[(i*slots+k)%subs.length].name)})):[];
 return <><h1>Plan de révision</h1><div className="card form"><label>Date de l’examen<input type="date" value={d} onChange={e=>{setD(e.target.value);localStorage.setItem('exam',e.target.value)}}/></label>
  <label>Heures par jour<input type="number" min="1" max="12" value={h} onChange={e=>setH(+e.target.value)}/></label></div>
  {plan.map((p,i)=><div key={i} className="card row"><b>{p.date}</b><span>{p.items.join(' · ')}</span></div>)}</>;
}
export function Profile(){
 const {user}=useContext(AuthCtx); const [f,setF]=useState({current:'',password:''}); const [m,setM]=useState('');
 const go=async e=>{e.preventDefault();try{await api.post('/me/password',f);setM('Mot de passe modifié ✔');setF({current:'',password:''})}catch(x){setM(x.response?.data?.message||'Erreur')}};
 return <><h1>Profil</h1><div className="card"><p><b>{user.fullName}</b><br/>{user.email}<br/><small>Rôle : {user.role}</small></p></div>
  <form className="card form" onSubmit={go}><h3>Changer le mot de passe</h3><input type="password" placeholder="Mot de passe actuel" value={f.current} onChange={e=>setF({...f,current:e.target.value})} required/><input type="password" placeholder="Nouveau mot de passe" value={f.password} onChange={e=>setF({...f,password:e.target.value})} required/><button className="btn">Enregistrer</button>{m&&<div className="ok">{m}</div>}</form></>;
}
export {Bars};
