import {useContext,useState} from 'react'; import {Link,useNavigate,useSearchParams} from 'react-router-dom'; import {api,AuthCtx} from './api';
const T={login:'Connexion',register:'Créer un compte',forgot:'Mot de passe oublié',reset:'Nouveau mot de passe'};
const EyeIcon=({open})=>open
 ?<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
 :<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/><path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/><line x1="1" y1="1" x2="23" y2="23"/></svg>;
export function Auth({mode}){
 const {login}=useContext(AuthCtx); const nav=useNavigate(); const [q]=useSearchParams();
 const [f,setF]=useState({fullName:'',email:'',password:''}); const [msg,setMsg]=useState(''); const [err,setErr]=useState(''); const [showPw,setShowPw]=useState(false);
 const set=k=>e=>setF({...f,[k]:e.target.value});
 const submit=async e=>{e.preventDefault();setErr('');setMsg('');
  try{
   if(mode==='login'||mode==='register'){const {data}=await api.post('/auth/'+mode,f);login(data);nav('/');}
   else if(mode==='forgot'){await api.post('/auth/forgot',{email:f.email});setMsg('Si ce compte existe, un email vient d\'être envoyé.');}
   else{await api.post('/auth/reset',{token:q.get('token'),password:f.password});setMsg('Mot de passe modifié. Vous pouvez vous connecter.');}
  }catch(x){setErr(x.response?.data?.message||x.message||'Erreur réseau — vérifiez que le serveur est démarré');}};
 return <div className="auth"><div className="auth-side"><h1>IASC Hub</h1><p>Vos cours du Master IASC, organisés et prêts à réviser.</p></div>
  <form className="auth-form" onSubmit={submit}><h2>{T[mode]}</h2>
   {mode==='register'&&<input placeholder="Nom complet" value={f.fullName} onChange={set('fullName')} required/>}
   {mode!=='reset'&&<input type="email" placeholder="Email (Gmail)" value={f.email} onChange={set('email')} required/>}
   {mode!=='forgot'&&<div className="pw-wrap"><input type={showPw?'text':'password'} placeholder="Mot de passe" value={f.password} onChange={set('password')} required/><button type="button" className="pw-eye" onClick={()=>setShowPw(v=>!v)} title={showPw?'Cacher':'Afficher'}><EyeIcon open={showPw}/></button></div>}
   {err&&<div className="err">{err}</div>}{msg&&<div className="ok">{msg}</div>}
   <button className="btn">{T[mode]}</button>
   <div className="links">{mode==='login'&&<><Link to="/register">Créer un compte</Link><Link to="/forgot">Mot de passe oublié ?</Link></>}{mode!=='login'&&<Link to="/login">← Connexion</Link>}</div>
  </form></div>;
}
