import {useEffect,useState} from 'react'; import {api,KINDS} from './api'; import {Bars} from './Shell';
const fmt=s=>s?new Date(s).toLocaleString('fr-FR'):'—';
export function Admin(){
 const [tab,setTab]=useState('users'); const [users,setUsers]=useState([]); const [st,setSt]=useState(null); const [subs,setSubs]=useState([]); const [msg,setMsg]=useState('');
 const [f,setF]=useState({subjectId:'',kind:KINDS[0],title:'',file:null});
 const load=()=>{api.get('/admin/users').then(r=>setUsers(r.data));api.get('/admin/stats').then(r=>setSt(r.data));api.get('/subjects').then(r=>setSubs(r.data));};
 useEffect(()=>{load();const i=setInterval(load,30000);return()=>clearInterval(i)},[]);
 const chpw=async u=>{const p=prompt('Nouveau mot de passe pour '+u.email+' (min 6) :'); if(!p) return; try{await api.put('/admin/users/'+u.id+'/password',{password:p});setMsg('Mot de passe modifié, email envoyé.')}catch(e){setMsg(e.response?.data?.message||'Erreur')}};
 const del=async u=>{if(confirm('Supprimer '+u.email+' ?')){try{await api.delete('/admin/users/'+u.id);load()}catch(e){setMsg(e.response?.data?.message||'Erreur')}}};
 const up=async e=>{e.preventDefault();const d=new FormData();d.append('subjectId',f.subjectId);d.append('kind',f.kind);d.append('title',f.title);d.append('file',f.file);setMsg('Envoi et extraction en cours…');
  try{await api.post('/admin/materials',d);setMsg('Contenu ajouté ✔');setF({...f,title:'',file:null});e.target.reset();load()}catch(x){setMsg(x.response?.data?.message||'Erreur')}};
 const dm=async m=>{if(confirm('Supprimer « '+m.title+' » ?')){await api.delete('/admin/materials/'+m.id);load()}};
 return <><h1>Administration</h1><div className="tabs">{[['users','Utilisateurs'],['content','Contenu'],['stats','Statistiques']].map(([k,l])=><button key={k} className={tab===k?'on':''} onClick={()=>setTab(k)}>{l}</button>)}</div>{msg&&<div className="ok">{msg}</div>}
 {tab==='users'&&<div className="card"><table className="t"><thead><tr><th>Nom</th><th>Email</th><th>Rôle</th><th>Statut</th><th>Dernière activité</th><th>Leçons</th><th/></tr></thead><tbody>
  {users.map(u=><tr key={u.id}><td>{u.fullName}</td><td>{u.email}</td><td>{u.role}</td><td><span className={'pt '+(u.online?'on':'')}/> {u.online?'En ligne':'Hors ligne'}</td><td>{fmt(u.lastSeen)}</td><td>{u.lessons}</td>
   <td><button className="btn sm" onClick={()=>chpw(u)}>Mot de passe</button> <button className="btn sm danger" onClick={()=>del(u)}>Supprimer</button></td></tr>)}</tbody></table></div>}
 {tab==='content'&&<><form className="card form" onSubmit={up}><h3>Ajouter un contenu</h3>
  <select required value={f.subjectId} onChange={e=>setF({...f,subjectId:e.target.value})}><option value="">— Matière —</option>{subs.map(s=><option key={s.id} value={s.id}>{s.name}</option>)}</select>
  <select value={f.kind} onChange={e=>setF({...f,kind:e.target.value})}>{KINDS.map(k=><option key={k}>{k}</option>)}</select>
  <input placeholder="Titre" required value={f.title} onChange={e=>setF({...f,title:e.target.value})}/>
  <input type="file" required accept=".docx,.pptx,.pdf,.txt,.md" onChange={e=>setF({...f,file:e.target.files[0]})}/><button className="btn">Téléverser</button></form>
  {subs.map(s=><div key={s.id} className="card"><h3>{s.name}</h3>{s.materials.map(m=><div key={m.id} className="row"><span>{m.kind} — {m.title}</span><button className="btn sm danger" onClick={()=>dm(m)}>Supprimer</button></div>)}{!s.materials.length&&<p className="muted">Vide</p>}</div>)}</>}
 {tab==='stats'&&st&&<><div className="grid3"><div className="card"><h4>Inscrits</h4><div className="big">{st.users}</div></div><div className="card"><h4>En ligne</h4><div className="big">{st.online}</div></div><div className="card"><h4>Ouvertures de leçons</h4><div className="big">{st.visits}</div></div></div>
  <div className="card"><h3>Activité (7 jours)</h3><Bars data={st.perDay}/></div><div className="card"><h3>Leçons les plus suivies</h3>{st.top.map((t,i)=><div key={i} className="row"><span>{t.title}</span><b>{t.count}</b></div>)}</div></>}</>;
}
