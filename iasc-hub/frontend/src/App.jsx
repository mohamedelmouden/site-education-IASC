import {useState} from 'react'; import {Routes,Route,Navigate} from 'react-router-dom';
import {AuthCtx} from './api'; import {Auth} from './Auth'; import {Shell,Dashboard,Stats,Plan,Profile} from './Shell'; import {Courses,Subject,Lesson} from './Content'; import {Admin} from './Admin';
export default function App(){
 const [user,setUser]=useState(()=>JSON.parse(localStorage.getItem('user')||'null'));
 const login=d=>{localStorage.setItem('token',d.token);localStorage.setItem('user',JSON.stringify(d.user));setUser(d.user)};
 const logout=()=>{localStorage.clear();setUser(null)};
 const pub=m=>user?<Navigate to="/" replace/>:<Auth mode={m}/>;
 return <AuthCtx.Provider value={{user,login,logout}}><Routes>
  <Route path="/login" element={pub('login')}/><Route path="/register" element={pub('register')}/>
  <Route path="/forgot" element={<Auth mode="forgot"/>}/><Route path="/reset" element={<Auth mode="reset"/>}/>
  <Route element={user?<Shell/>:<Navigate to="/login" replace/>}>
   <Route index element={<Dashboard/>}/><Route path="courses" element={<Courses/>}/><Route path="courses/:id" element={<Subject/>}/>
   <Route path="lesson/:id" element={<Lesson/>}/><Route path="stats" element={<Stats/>}/><Route path="plan" element={<Plan/>}/><Route path="profile" element={<Profile/>}/>
   <Route path="admin" element={user?.role==='ADMIN'?<Admin/>:<Navigate to="/" replace/>}/>
  </Route></Routes></AuthCtx.Provider>;
}
