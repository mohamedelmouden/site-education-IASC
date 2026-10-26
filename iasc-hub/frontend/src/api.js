import axios from 'axios'; import {createContext} from 'react';
export const AuthCtx=createContext(null);
export const api=axios.create({baseURL:'http://localhost:8080/api'});
api.interceptors.request.use(c=>{const t=localStorage.getItem('token'); if(t) c.headers.Authorization='Bearer '+t; return c;});
api.interceptors.response.use(r=>r,e=>{ if(e.response?.status===401){const authPages=['/login','/register','/forgot','/reset']; const onAuthPage=authPages.some(p=>location.pathname.startsWith(p)); if(!onAuthPage){localStorage.clear();location.href='/login';}} return Promise.reject(e);});
export const download=async(url,name)=>{const r=await api.get(url,{responseType:'blob'});const a=document.createElement('a');a.href=URL.createObjectURL(r.data);a.download=name;a.click();};
export const KINDS=['Cours','Série d\'exercices','Correction','TP','Autre'];
