const root=document.documentElement,saved=localStorage.getItem('theme');if(saved)root.dataset.theme=saved;
document.getElementById('theme').onclick=()=>{const d=root.dataset.theme?root.dataset.theme==='dark':matchMedia('(prefers-color-scheme: dark)').matches;root.dataset.theme=d?'light':'dark';localStorage.setItem('theme',root.dataset.theme)};
const pal={fg:['#d9822b','#e09a4a','#cf6f2c','#e8a95e'],bg:['#8fae5a','#a3b86a','#7f9e55','#b3c27a','#96a85f']};
function plate(c,num){const x=c.getContext('2d'),W=c.width,R=W/2;x.clearRect(0,0,W,W);
 const m=document.createElement('canvas');m.width=m.height=W;const mx=m.getContext('2d');mx.fillStyle='#000';mx.font=`bold ${W*.55}px system-ui,sans-serif`;mx.textAlign='center';mx.textBaseline='middle';mx.fillText(num,R,R*1.05);
 const md=mx.getImageData(0,0,W,W).data,dots=[];let tries=0;
 while(dots.length<1400&&tries<20000){tries++;const r=W*(.008+Math.random()*.022),a=Math.random()*6.283,d=Math.sqrt(Math.random())*(R-r-2),px=R+Math.cos(a)*d,py=R+Math.sin(a)*d;
  if(dots.some(o=>(o[0]-px)**2+(o[1]-py)**2<(o[2]+r+1)**2))continue;dots.push([px,py,r])}
 for(const[px,py,r]of dots){const on=md[((py|0)*W+(px|0))*4+3]>128,p=on?pal.fg:pal.bg;x.fillStyle=p[Math.random()*p.length|0];x.beginPath();x.arc(px,py,r,0,6.283);x.fill()}}
let n=0;const demo=document.getElementById('demo'),ans=document.getElementById('ans');
function nextPlate(){n=[12,29,45,57,74,6,8,3,5,42][Math.random()*10|0];plate(demo,n);ans.textContent=''}
document.getElementById('newplate').onclick=nextPlate;document.getElementById('reveal').onclick=()=>ans.textContent=`The number was ${n}.`;
plate(document.getElementById('hero'),74);nextPlate();
// Machado 2009 severity-1.0 matrices (linear RGB)
const M={protan:[.152286,1.052583,-.204868,.114503,.786281,.099216,-.003882,-.048116,1.051998],deutan:[.367322,.860646,-.227968,.280085,.672501,.047413,-.01182,.04294,.968881],tritan:[1.255528,-.076749,-.178779,-.078411,.930809,.147602,.004733,.691367,.3039]};
const lin=v=>(v/=255)<=.04045?v/12.92:((v+.055)/1.055)**2.4,gam=v=>255*Math.min(1,Math.max(0,v<=.0031308?12.92*v:1.055*v**(1/2.4)-.055));
const sw=['#e41a1c','#4daf4a','#ff7f00','#984ea3','#377eb8','#a65628','#f781bf','#ffff33','#1b9e77','#d95f02','#e7298a','#66a61e'];
function sim(){const t=document.querySelector('[name=cvd]:checked').value,s=document.getElementById('sev').value/100,c=document.getElementById('sim'),x=c.getContext('2d'),w=c.width/6,h=c.height/2;
 sw.forEach((hx,i)=>{let rgb=[1,3,5].map(k=>lin(parseInt(hx.substr(k,2),16)));if(t!=='none'){const m=M[t],o=[0,1,2].map(r=>m[r*3]*rgb[0]+m[r*3+1]*rgb[1]+m[r*3+2]*rgb[2]);rgb=rgb.map((v,k)=>v*(1-s)+o[k]*s)}
  x.fillStyle=`rgb(${rgb.map(gam).join()})`;x.fillRect((i%6)*w,(i/6|0)*h,w-4,h-4)})}
document.querySelectorAll('[name=cvd]').forEach(e=>e.onchange=sim);document.getElementById('sev').oninput=sim;sim();
