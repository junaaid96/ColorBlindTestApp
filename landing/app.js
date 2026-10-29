'use strict';
const $=s=>document.querySelector(s),$$=s=>document.querySelectorAll(s),root=document.documentElement;
root.classList.add('js');
/* theme */
const tb=$('#theme');const isDark=()=>root.dataset.theme?root.dataset.theme==='dark':matchMedia('(prefers-color-scheme: dark)').matches;
const syncTheme=()=>{tb.setAttribute('aria-pressed',String(isDark()));tb.setAttribute('aria-label',isDark()?'Switch to light theme':'Switch to dark theme')};
tb.addEventListener('click',()=>{root.dataset.theme=isDark()?'light':'dark';try{localStorage.setItem('theme',root.dataset.theme)}catch(e){}syncTheme()});syncTheme();
/* mobile menu */
const mb=$('#menubtn'),menu=$('#menu');
mb.addEventListener('click',()=>{const o=menu.classList.toggle('open');mb.setAttribute('aria-expanded',String(o))});
menu.addEventListener('click',e=>{if(e.target.tagName==='A'){menu.classList.remove('open');mb.setAttribute('aria-expanded','false')}});
/* OS detection */
const R='https://github.com/junaaid96/color-vision-lab/releases/download/v3.0.1/';
const OS={windows:['Download for Windows','Installer .exe · 2.4 MB',R+'Color.Vision.Lab_3.0.1_x64-setup.exe'],
mac:['Download for macOS','Universal .dmg · Apple silicon + Intel · 6 MB',R+'Color.Vision.Lab_3.0.1_universal.dmg'],
linux:['Download for Linux','AppImage · also .deb and .rpm',R+'Color.Vision.Lab_3.0.1_amd64.AppImage'],
android:['Download for Android','Signed APK · 23 MB',R+'ColorVisionLab-v3.0.1-app-universal-release.apk'],
ios:['Open the web app','No App Store build yet · add to Home Screen','app/']};
function detect(){const ua=navigator.userAgent,p=(navigator.userAgentData&&navigator.userAgentData.platform)||navigator.platform||'';
 if(/android/i.test(ua))return'android';if(/iphone|ipad|ipod/i.test(ua)||(/Mac/.test(p)&&navigator.maxTouchPoints>1))return'ios';
 if(/win/i.test(p)||/windows/i.test(ua))return'windows';if(/mac/i.test(p)||/mac os/i.test(ua))return'mac';if(/linux|x11|cros/i.test(p+ua))return'linux';return null}
const os=detect();if(os){const[l,s,h]=OS[os],a=$('#primary-dl');$('#dl-label').textContent=l;$('#dl-sub').textContent=s;a.href=h;if(h==='app/'){a.target='_blank';a.rel='noopener'}const c=document.querySelector(`[data-os="${os}"]`);if(c)c.classList.add('mine')}
/* colour maths: Machado 2009, severity 1.0, linear RGB */
const M={protan:[.152286,1.052583,-.204868,.114503,.786281,.099216,-.003882,-.048116,1.051998],deutan:[.367322,.860646,-.227968,.280085,.672501,.047413,-.01182,.04294,.968881],tritan:[1.255528,-.076749,-.178779,-.078411,.930809,.147602,.004733,.691367,.3039]};
const LIN=new Float32Array(256);for(let i=0;i<256;i++){const v=i/255;LIN[i]=v<=.04045?v/12.92:((v+.055)/1.055)**2.4}
const GAM=new Uint8ClampedArray(4096);for(let i=0;i<4096;i++){const v=i/4095;GAM[i]=Math.round(255*(v<=.0031308?12.92*v:1.055*v**(1/2.4)-.055))}
const g=v=>GAM[v<=0?0:v>=1?4095:(v*4095)|0];
function matrix(t,s){const I=[1,0,0,0,1,0,0,0,1];return I.map((v,i)=>v*(1-s)+M[t][i]*s)}
/* plates */
const pal={fg:['#d9822b','#e09a4a','#cf6f2c','#e8a95e'],bg:['#8fae5a','#a3b86a','#7f9e55','#b3c27a','#96a85f']};
function plate(c,num){const x=c.getContext('2d'),W=c.width,R2=W/2;x.clearRect(0,0,W,W);
 const m=document.createElement('canvas');m.width=m.height=W;const mx=m.getContext('2d');mx.font=`bold ${W*.55}px system-ui,sans-serif`;mx.textAlign='center';mx.textBaseline='middle';mx.fillText(num,R2,R2*1.05);
 const md=mx.getImageData(0,0,W,W).data,dots=[];let tries=0;
 while(dots.length<1500&&tries<20000){tries++;const r=W*(.008+Math.random()*.022),a=Math.random()*6.283,d=Math.sqrt(Math.random())*(R2-r-2),px=R2+Math.cos(a)*d,py=R2+Math.sin(a)*d;
  if(dots.some(o=>(o[0]-px)**2+(o[1]-py)**2<(o[2]+r+1)**2))continue;dots.push([px,py,r])}
 for(const[px,py,r]of dots){const on=md[((py|0)*W+(px|0))*4+3]>128,p=on?pal.fg:pal.bg;x.fillStyle=p[Math.random()*p.length|0];x.beginPath();x.arc(px,py,r,0,6.283);x.fill()}}
let n=0;const demo=$('#demo'),ans=$('#ans');
function nextPlate(){n=[12,29,45,57,74,6,8,3,5,42][Math.random()*10|0];plate(demo,n);ans.textContent=''}
$('#newplate').addEventListener('click',nextPlate);$('#reveal').addEventListener('click',()=>{ans.textContent=`The number was ${n}.`});
const idle=window.requestIdleCallback||(f=>setTimeout(f,1));
idle(()=>{plate($('#hero'),74);nextPlate()});
/* simulator */
const sc=$('#simc'),sx=sc.getContext('2d',{willReadFrequently:true});let src=null,userImg=null;
function drawScene(name){const c=document.createElement('canvas');c.width=sc.width;c.height=sc.height;const x=c.getContext('2d'),W=c.width,H=c.height;
 if(name==='fruit'){const bg=x.createLinearGradient(0,0,0,H);bg.addColorStop(0,'#cfe8f7');bg.addColorStop(.62,'#9fd0ea');bg.addColorStop(.62,'#4f8a3c');bg.addColorStop(1,'#2f6b2a');x.fillStyle=bg;x.fillRect(0,0,W,H);
  x.fillStyle='#8b5a2b';x.fillRect(W*.08,H*.55,W*.84,H*.08);
  const fr=[['#d7263d',.17,.45,52],['#f46036',.32,.43,48],['#ffd23f',.47,.44,46],['#2e7d32',.62,.45,50],['#7bc043',.77,.44,46],['#9b2c9b',.88,.46,40],['#e83f6f',.24,.75,44],['#3bceac',.42,.78,40],['#0ead69',.6,.76,46],['#ee4266',.78,.77,42]];
  for(const[c2,px,py,r]of fr){x.fillStyle=c2;x.beginPath();x.arc(W*px,H*py,r,0,6.283);x.fill();x.fillStyle='rgba(255,255,255,.35)';x.beginPath();x.arc(W*px-r*.35,H*py-r*.35,r*.25,0,6.283);x.fill();x.fillStyle='#3d6b1f';x.beginPath();x.ellipse(W*px+6,H*py-r-4,10,5,-.5,0,6.283);x.fill()}
  x.fillStyle='#ffffff';x.font='bold 22px system-ui,sans-serif';x.textAlign='right';x.fillText('Ripe or not?',W-20,H-20);x.textAlign='left'}
 else if(name==='chart'){x.fillStyle='#ffffff';x.fillRect(0,0,W,H);const cols=['#e41a1c','#4daf4a','#377eb8','#ff7f00','#984ea3'],labels=['Mon','Tue','Wed','Thu','Fri','Sat'];x.font='16px system-ui,sans-serif';x.fillStyle='#333';
  x.strokeStyle='#ccc';for(let i=0;i<5;i++){x.beginPath();x.moveTo(60,60+i*80);x.lineTo(W-150,60+i*80);x.stroke()}
  cols.forEach((c2,k)=>{x.strokeStyle=c2;x.lineWidth=5;x.beginPath();labels.forEach((l,i)=>{const px=70+i*((W-230)/5),py=380-((Math.sin(i*1.3+k*1.7)+1)*120+k*20);i?x.lineTo(px,py):x.moveTo(px,py)});x.stroke();x.fillStyle=c2;x.fillRect(W-130,70+k*36,22,22);x.fillStyle='#333';x.fillText('Series '+(k+1),W-100,87+k*36)});
  labels.forEach((l,i)=>x.fillText(l,58+i*((W-230)/5),410))}
 else{const sw=['#e41a1c','#4daf4a','#ff7f00','#984ea3','#377eb8','#a65628','#f781bf','#ffff33','#1b9e77','#d95f02','#7570b3','#e7298a','#66a61e','#e6ab02','#000000','#56b4e9','#009e73','#f0e442'];
  const cw=W/6,ch=H/3;sw.forEach((c2,i)=>{x.fillStyle=c2;x.fillRect((i%6)*cw+4,(i/6|0)*ch+4,cw-8,ch-8)})}
 return x.getImageData(0,0,W,H)}
function render(){if(!src)return;const t=$('[name=cvd]:checked').value,s=$('#sev').value/100,split=$('#split').checked,o=new ImageData(src.width,src.height),d=src.data,od=o.data;
 const m=t==='none'?null:matrix(t,s),half=split?src.width/2:0;
 for(let i=0,p=0;i<d.length;i+=4,p++){if(!m||(split&&(p%src.width)<half)){od[i]=d[i];od[i+1]=d[i+1];od[i+2]=d[i+2]}else{const r=LIN[d[i]],gg=LIN[d[i+1]],b=LIN[d[i+2]];od[i]=g(m[0]*r+m[1]*gg+m[2]*b);od[i+1]=g(m[3]*r+m[4]*gg+m[5]*b);od[i+2]=g(m[6]*r+m[7]*gg+m[8]*b)}od[i+3]=255}
 sx.putImageData(o,0,0);if(split&&m){sx.fillStyle='#fff';sx.fillRect(half-1,0,2,src.height)}
 const names={none:'Typical vision',protan:'Protan (red-weak)',deutan:'Deutan (green-weak)',tritan:'Tritan (blue-yellow)'},label=names[t]+(m?` · ${Math.round(s*100)}%`:'');
 $('#simtag').textContent=label;sc.setAttribute('aria-label',`Sample image shown with ${label}`);$('#sevo').textContent=Math.round(s*100)+'%'}
function loadScene(){const v=$('[name=scene]:checked').value;src=drawScene(v);render()}
$$('[name=cvd]').forEach(e=>e.addEventListener('change',render));$('#sev').addEventListener('input',render);$('#split').addEventListener('change',render);
$$('[name=scene]').forEach(e=>e.addEventListener('change',loadScene));
$('#photo').addEventListener('change',e=>{const f=e.target.files&&e.target.files[0];if(!f)return;const img=new Image();img.onload=()=>{const W=sc.width,H=sc.height,c=document.createElement('canvas');c.width=W;c.height=H;const x=c.getContext('2d'),k=Math.max(W/img.width,H/img.height);x.drawImage(img,(W-img.width*k)/2,(H-img.height*k)/2,img.width*k,img.height*k);src=x.getImageData(0,0,W,H);URL.revokeObjectURL(img.src);$$('[name=scene]').forEach(r=>r.checked=false);render()};img.src=URL.createObjectURL(f)});
idle(loadScene);
/* reveal on scroll */
if('IntersectionObserver'in window&&!matchMedia('(prefers-reduced-motion: reduce)').matches){const io=new IntersectionObserver(es=>es.forEach(e=>{if(e.isIntersecting){e.target.classList.add('in');io.unobserve(e.target)}}),{rootMargin:'0px 0px -8% 0px'});$$('.reveal').forEach(el=>io.observe(el))}else $$('.reveal').forEach(el=>el.classList.add('in'));
/* screenshot gallery: device tabs, theme toggle, keyboard lightbox */
(()=>{const box=document.getElementById('shots');if(!box)return;const tabs=[...document.querySelectorAll('[data-dev]')],ths=[...document.querySelectorAll('[data-th]')];
let dev='desktop',th=document.documentElement.dataset.theme==='dark'||(!document.documentElement.dataset.theme&&matchMedia('(prefers-color-scheme: dark)').matches)?'dark':'light';
const all=[...box.querySelectorAll('.shot')];const vis=()=>all.filter(f=>!f.hidden);
function apply(){box.dataset.device=dev;all.forEach(f=>f.hidden=!(f.dataset.device===dev&&f.dataset.theme===th));tabs.forEach(b=>{const on=b.dataset.dev===dev;b.setAttribute('aria-selected',String(on));b.tabIndex=on?0:-1});ths.forEach(b=>b.setAttribute('aria-pressed',String(b.dataset.th===th)))}
tabs.forEach((b,i)=>{b.addEventListener('click',()=>{dev=b.dataset.dev;apply()});b.addEventListener('keydown',e=>{const d=e.key==='ArrowRight'?1:e.key==='ArrowLeft'?-1:0;if(!d)return;e.preventDefault();const n=tabs[(i+d+tabs.length)%tabs.length];dev=n.dataset.dev;apply();n.focus()})});
ths.forEach(b=>b.addEventListener('click',()=>{th=b.dataset.th;apply()}));apply();
const lb=document.getElementById('lightbox');if(!lb||!lb.showModal)return;const li=lb.querySelector('img'),lc=lb.querySelector('figcaption');let cur=0,opener=null;
function show(i){const v=vis();cur=(i+v.length)%v.length;const im=v[cur].querySelector('img');li.src=im.currentSrc||im.src;li.alt=im.alt;lc.textContent=v[cur].querySelector('figcaption').textContent+' · '+(cur+1)+' / '+v.length}
box.addEventListener('click',e=>{const b=e.target.closest('.shot-open');if(!b)return;opener=b;show(vis().indexOf(b.parentElement));lb.showModal();lb.querySelector('.lb-close').focus()});
lb.querySelector('.lb-prev').addEventListener('click',()=>show(cur-1));lb.querySelector('.lb-next').addEventListener('click',()=>show(cur+1));lb.querySelector('.lb-close').addEventListener('click',()=>lb.close());
lb.addEventListener('keydown',e=>{if(e.key==='ArrowLeft')show(cur-1);if(e.key==='ArrowRight')show(cur+1)});
lb.addEventListener('click',e=>{if(e.target===lb)lb.close()});lb.addEventListener('close',()=>{li.removeAttribute('src');opener&&opener.focus()})})();
