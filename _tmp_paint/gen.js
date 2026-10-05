// 米哼猪尸 64x64 贴图生成器：大米主题（糯米浆白腐皮 / 金稻穗金剑 / 稻秆腰裙 / 米白骨点 / 酱油黑眼）
const zlib = require('zlib');
const fs = require('fs');
const path = require('path');

// ---------------- 数据：43 块 from/to ----------------
const BOXES = [
 ['torso',-4.5,12,-2.5,4.5,23,2.5],['pelvis',-4,9.2,-2.2,4,12.6,2.2],
 ['neck',-1.6,22.6,-1.2,1.6,24.6,1.4],['chest_l',-4.1,17.4,-2.85,-0.4,20.4,-2.5],
 ['chest_r',0.4,17.4,-2.85,4.1,20.4,-2.5],['loin_front',-2.7,6.4,-2.6,2.7,9.3,-2.1],
 ['loin_side_l',-4.45,6.8,-1.7,-3.85,9.7,1.7],['loin_side_r',3.85,6.8,-1.7,4.45,9.7,1.7],
 ['spine1',-0.8,16,2.45,0.8,17.2,2.95],['spine2',-0.7,18.3,2.45,0.7,19.4,2.95],
 ['spine3',-0.6,20.4,2.45,0.6,21.4,2.95],
 ['skull',-4,24,-4,4,32,4],['cheek_l',-4.55,25,-2.4,-4,29,1.4],['cheek_r',4,25,-2.4,4.55,29,1.4],
 ['snout_upper',-2.6,26.2,-6.2,2.6,29.4,-4],['nose_tip',-2.1,26.6,-7.1,2.1,29,-6.2],
 ['jaw_lower',-2.3,24.7,-5.6,2.3,26.2,-4],['brow_l',-3.2,29,-5.7,-0.7,30,-4.2],
 ['brow_r',0.7,29,-5.7,3.2,30,-4.2],['tusk_l',-2.55,25.3,-6.1,-1.55,26.7,-5.4],
 ['tusk_r',1.55,25.5,-6.1,2.55,26.5,-5.4],['ear_l_droop',-6.1,29.2,-1.9,-4.2,32.2,-0.2],
 ['ear_r_droop',4.2,29.4,-1.9,6.1,31.9,-0.2],['decay_bump',-3.3,30.2,-1.2,-2.2,31.2,0.1],
 ['upper_arm_r',-8.2,15.8,-2.3,-4.4,22,2.3],['forearm_r',-8.3,9.6,-2.3,-4.3,16.2,2.3],
 ['fist_r',-8,8.6,-2.6,-4.4,10.2,2.6],['sword_grip',-6.9,7,-1.1,-5.5,9.8,0.1],
 ['sword_guard',-7.5,9.3,-1.8,-4.9,10.3,0.8],['sword_blade',-6.8,1.3,-1,-5.6,9.4,0],
 ['sword_tip',-6.7,0.3,-1,-5.5,1.3,0],['upper_arm_l',4.4,15.8,-2.3,8.2,22,2.3],
 ['forearm_l',4.3,9.6,-2.3,8.3,16.2,2.3],['fist_l',4.4,8.6,-2.6,8,10.2,2.6],
 ['thigh_r',-4.1,6,-2.3,-0.3,12,2.3],['shin_r',-3.9,1.6,-2.1,-0.5,6.4,2.1],
 ['hoof_r',-4.2,0,-3.2,-0.4,1.7,2.3],['thigh_l',0.3,6,-2.3,4.1,12,2.3],
 ['shin_l',0.5,1.6,-2.1,3.9,6.4,2.1],['hoof_l',0.4,0,-3.2,4.2,1.7,2.3],
 ['tail_base',-0.9,17.8,2.45,0.9,19.3,3.75],['tail_curl',0.35,18.9,3.45,1.7,20.1,4.45],
 ['tail_tip',1.3,19.8,4.1,2.45,20.95,4.95]
];
const C = {};
const Cb = BOXES.map(b=>{const c={n:b[0],fx:b[1],fy:b[2],fz:b[3],tx:b[4],ty:b[5],tz:b[6]};
 c.w=Math.floor(Math.round((c.tx-c.fx)*1000)/1000);c.h=Math.floor(Math.round((c.ty-c.fy)*1000)/1000);c.d=Math.floor(Math.round((c.tz-c.fz)*1000)/1000);
 c.W=2*c.d+2*c.w;c.H=c.d+c.h;C[c.n]=c;return c;});

// ---------------- 货架装箱 64 宽 ----------------
const order = Cb.slice().sort((a,b)=>b.H-a.H||b.W-a.W);
let x=0,y=0,sh=0;
for(const c of order){ if(c.W===0&&c.H===0) continue;
  if(x+c.W>64){x=0;y+=sh;sh=0;}
  c.ox=x;c.oy=y;x+=c.W;sh=Math.max(sh,c.H);
}
// 六个面 UV（blockbench box net 约定，[u1,v1,u2,v2]，up/down 为反向坐标）
function faces(c){const{ox:o,oy:p,w,h,d}=c;return{
 north:[o+d,p+d,o+d+w,p+d+h], south:[o+2*d+w,p+d,o+2*d+2*w,p+d+h],
 east:[o,p+d,o+d,p+d+h], west:[o+d+w,p+d,o+2*d+w,p+d+h],
 up:[o+d,p+d,o+d+w,p], down:[o+d+w,p,o+d,p+d]};}
for(const c of Cb){c.f=faces(c);
 // 亚像素面重定向到最大有效面
 const area=r=>Math.abs(r[2]-r[0])*Math.abs(r[3]-r[1]);
 let best='north',ba=0;for(const k of ['north','south','east','west','up','down']){const a=area(c.f[k]);if(a>ba){ba=a;best=k;}}
 for(const k of ['north','south','east','west','up','down']){const r=c.f[k];if(Math.abs(r[2]-r[0])<1||Math.abs(r[3]-r[1])<1)c.f[k]=c.f[best].slice();}
}

// ---------------- 画布 ----------------
const W=64,H=64;const img=Buffer.alloc(W*H*4); // 全透明
const px=(u,v,col)=>{u=Math.round(u);v=Math.round(v);if(u<0||v<0||u>=W||v>=H)return;const i=(v*W+u)*4;img[i]=col[0];img[i+1]=col[1];img[i+2]=col[2];img[i+3]=col[3]===undefined?255:col[3];};
const rect=(r,col)=>{for(let v=Math.min(r[1],r[3]);v<Math.max(r[1],r[3]);v++)for(let u=Math.min(r[0],r[2]);u<Math.max(r[0],r[2]);u++)px(u,v,col);};
const inRect=(r,lx,ly)=>lx>=0&&ly>=0&&lx<Math.abs(r[2]-r[0])&&ly<Math.abs(r[3]-r[1]);
const at=(r,lx,ly,col)=>px(r[0]+lx,r[1]+ly,col);
// 世界坐标 -> north/south/east 面内像素
const pn=(c,wx,wy,col)=>at(c.f.north,Math.round(wx-c.fx),c.ty-1-Math.round(wy),col);
const ps=(c,wx,wy,col)=>at(c.f.south,Math.round(wx-c.fx),c.ty-1-Math.round(wy),col);
const pe=(c,wz,wy,col)=>at(c.f.east,Math.round(wz-c.fz),c.ty-1-Math.round(wy),col);
const pw=(c,wz,wy,col)=>at(c.f.west,Math.round(wz-c.fz),c.ty-1-Math.round(wy),col);

// ---------------- 配色（大米主题） ----------------
const SKIN=[231,221,204],SKIN_L=[241,233,217],SKIN_S=[206,192,169],
 ROT1=[169,178,150],ROT2=[139,149,121],ROT_D=[104,112,90],
 PINK=[227,197,181],PINK_S=[205,172,154],BONE=[237,231,209],BONE_S=[210,200,170],
 DARK=[58,46,36],EYE=[28,22,18],MOUTH=[72,54,42],
 CLOTH=[178,146,96],CLOTH_L=[199,167,113],CLOTH_S=[139,109,69],CLOTH_D=[103,79,49],
 HOOF=[84,64,46],HOOF_S=[59,43,29],HOOF_H=[128,99,70],
 GOLD=[240,196,72],GOLD_L=[253,239,166],GOLD_S=[183,135,39],
 GRIP=[107,74,42],GRIP_S=[80,53,29],TUSK=[243,236,213],TUSK_S=[214,205,178],
 GRAIN=[247,240,220],DIRT=[107,92,70];

function hsh(s){let h=2166136261;for(let i=0;i<s.length;i++){h^=s.charCodeAt(i);h=Math.imul(h,16777619);}return h>>>0;}
function rng(seed){let a=seed>>>0;return()=>{a|=0;a=a+0x6D2B79F5|0;let t=Math.imul(a^a>>>15,1|a);t=t+Math.imul(t^t>>>7,61|t)^t;return((t^t>>>14)>>>0)/4294967296;};}

// 通用腐皮填充：底色 + 竖向微光 + 腐斑 + 暗边
function skin(c,face,o={}){const r=c.f[face],w=Math.abs(r[2]-r[0]),h=Math.abs(r[3]-r[1]);
 let base=SKIN,light=SKIN_L,shade=SKIN_S;
 if(o.style==='nose'){base=[236,219,206];light=[243,229,217];shade=[219,201,186];}
 if(o.style==='blush'){base=SKIN;light=SKIN_L;shade=[220,204,188];}
 const rnd=rng(hsh(c.n+face));const blobs=[];const nb=o.blobs!==undefined?o.blobs:(h>=4?(rnd()<0.35?2:1):(rnd()<0.3?1:0));
 for(let i=0;i<nb;i++)blobs.push([1+Math.floor(rnd()*Math.max(1,w-2)),1+Math.floor(rnd()*Math.max(1,h-2)),0]);
 for(let ly=0;ly<h;ly++)for(let lx=0;lx<w;lx++){let col=base;const q=rnd();
  if(ly===0&&q<0.45)col=light;else if(ly===h-1&&q<0.6)col=shade;else if(q<0.07)col=shade;else if(q<0.12)col=light;
  if(o.style==='blush'&&q>0.92)col=PINK;
  for(const[bx,by,br]of blobs){const d=Math.max(Math.abs(lx-bx),Math.abs(ly-by));if(d<=br){col=br===0?ROT2:(d===0?ROT2:ROT1);}}
  if(q>0.985&&o.style!=='nose')col=GRAIN;
  at(r,lx,ly,col);}
}
function clothFace(c,face){const r=c.f[face],w=Math.abs(r[2]-r[0]),h=Math.abs(r[3]-r[1]);
 for(let ly=0;ly<h;ly++)for(let lx=0;lx<w;lx++){let col=CLOTH;
  if(ly===0)col=CLOTH_L; if(ly===h-1)col=((lx%2===0)?CLOTH_D:CLOTH_S);
  if(lx===Math.floor(w/2))col=CLOTH_S;
  at(r,lx,ly,col);}
 // 米粒印
 if(w>=4){at(r,1,0,GRAIN);at(r,w-2,0,GRAIN);}
}
function hoof(c,face){const r=c.f[face],w=Math.abs(r[2]-r[0]),h=Math.abs(r[3]-r[1]);
 for(let ly=0;ly<h;ly++)for(let lx=0;lx<w;lx++){let col=HOOF;if(ly===0)col=HOOF_H;if(ly===h-1)col=HOOF_S;at(r,lx,ly,col);}}

// ---------------- 逐块绘制 ----------------
for(const c of Cb){const n=c.n;
 if(['upper_arm_r','forearm_r','upper_arm_l','forearm_l','thigh_r','thigh_l','shin_r','shin_l','pelvis'].includes(n)){
  skin(c,'north');skin(c,'south');skin(c,'east',{blobs:1});skin(c,'west',{blobs:1});skin(c,'up',{blobs:0});skin(c,'down',{blobs:0});
 } else if(n==='skull'){
  skin(c,'north',{blobs:0});skin(c,'south',{blobs:2});skin(c,'east',{blobs:1});skin(c,'west',{blobs:1});skin(c,'up',{blobs:2});skin(c,'down',{blobs:0});
 } else if(n==='neck'){
  for(const f of ['north','south','east','west','up','down'])skin(c,f,{blobs:0});
 } else if(n==='torso'){
  skin(c,'north',{blobs:0});skin(c,'south',{blobs:2});skin(c,'east',{blobs:1});skin(c,'west',{blobs:1});skin(c,'up',{blobs:1});skin(c,'down',{blobs:0});
 } else { // 其它小块默认腐皮，后面特例覆盖
  for(const f of ['north','south','east','west','up','down'])skin(c,f,{blobs:0});
 }
}
// --- 头 ---
const sk=C.skull;
// 眼窝 + 酱油黑眼 + 米白高光
for(const s of [-1,1]){for(let wy=31;wy<=31;wy++)for(let wx=s*2;s<0?wx>=-3:wx<=3;wx+=s<0?-1:1){pn(sk,wx,wy,SKIN_S);}
 pn(sk,s*2,30,EYE);pn(sk,s*2,31,SKIN);}
// 额头腐斑、下巴米粒
pn(sk,-3,31,ROT1);pn(sk,-3,25,ROT2);pn(sk,0,25,GRAIN);
// 头顶斑秃
rect(sk.f.up,SKIN);{const r=sk.f.up;at(r,2,2,ROT1);at(r,3,2,ROT2);at(r,2,3,ROT2);at(r,5,4,ROT1);at(r,1,5,GRAIN);}
// 猪鼻：鼻筒只比肤色略粉，鼻头粉一些
for(const f of ['north','south','east','west','up'])skin(C.snout_upper,f,{style:'blush',blobs:0});
for(const f of ['north','south','east','west','up','down'])skin(C.nose_tip,f,{style:'nose',blobs:0});
pn(C.nose_tip,-1,28,DARK);pn(C.nose_tip,1,28,DARK);pn(C.nose_tip,0,28,[243,226,213]);
pn(C.nose_tip,-1,27,PINK_S);pn(C.nose_tip,1,27,PINK_S);
// 下巴：肤色，顶缘一条深嘴缝
for(const f of ['north','south'])skin(C.jaw_lower,f,{style:'blush',blobs:0});
for(const f of ['east','west'])skin(C.jaw_lower,f,{style:'blush',blobs:0});
at(C.jaw_lower.f.north,1,0,MOUTH);at(C.jaw_lower.f.north,2,0,MOUTH);
at(C.jaw_lower.f.south,1,0,MOUTH);at(C.jaw_lower.f.south,2,0,MOUTH);
// 垂腮
skin(C.cheek_l,'north',{style:'blush',blobs:0});skin(C.cheek_r,'north',{style:'blush',blobs:0});
pn(C.cheek_l,-4,27,ROT1);pn(C.cheek_r,4,26,ROT1);
// 眉毛深棕
for(const c of [C.brow_l,C.brow_r])for(const f of ['north','south','east','west','up','down'])rect(c.f[f],DARK);
// 獠牙象牙白
for(const c of [C.tusk_l,C.tusk_r]){for(const f of ['north','south'])rect(c.f[f],TUSK);
 at(c.f.north,0,Math.abs(c.f.north[3]-c.f.north[1])-1,TUSK_S);}
// 耳朵：外粉皮 + 耳尖腐黑
for(const c of [C.ear_r_droop,C.ear_l_droop]){const r=c.f.north,h=Math.abs(r[3]-r[1]);
 for(let ly=0;ly<h;ly++){let col=ly>=h-1?ROT_D:(ly===0?SKIN:PINK);for(let lx=0;lx<Math.abs(r[2]-r[0]);lx++)at(r,lx,ly,col);}
 const r2=c.f.south;for(let lx=0;lx<Math.abs(r2[2]-r2[0]);lx++)at(r2,lx,Math.abs(r2[3]-r2[1])-1,ROT_D);}
// 头顶腐肉块
for(const f of ['north','south','east','west','up','down'])rect(C.decay_bump.f[f],ROT2);
at(C.decay_bump.f.north,0,0,ROT_D);
// --- 躯干细节 ---
const t=C.torso;
// 正面：左肋一处小腐洞、一道腹线、锁骨阴影、米粒
for(let wx=-1;wx<=2;wx++)pn(t,wx,14,SKIN_S);
pn(t,-2,14,ROT2);pn(t,-3,15,ROT1);pn(t,-2,15,ROT1);
pn(t,-3,21,SKIN_S);pn(t,3,21,SKIN_S);pn(t,-1,13,GRAIN);pn(t,2,19,GRAIN);
// 背面脊柱暗缝 + 腐斑
for(let wy=13;wy<=21;wy++)ps(t,0,wy,SKIN_S);
ps(t,-3,14,ROT1);ps(t,3,18,ROT2);ps(t,-2,20,ROT1);
// 胸肌干净，只加体积阴影
pn(C.chest_l,1,0,SKIN_L);pn(C.chest_l,0,2,SKIN_S);pn(C.chest_l,2,2,SKIN_S);
pn(C.chest_r,1,0,SKIN_L);pn(C.chest_r,2,2,SKIN_S);pn(C.chest_r,0,2,SKIN_S);
// 四肢腐皮特例：拳头脏污、大腿露骨
for(const n of ['fist_r','fist_l']){const c=C[n];
 for(const f of ['north','south','east','west'])skin(c,f,{blobs:1});
 const r=c.f.north,h=Math.abs(r[3]-r[1]);for(let lx=0;lx<Math.abs(r[2]-r[0]);lx++)at(r,lx,h-1,DIRT);}
pn(C.thigh_r,-2,9,BONE);pn(C.thigh_r,-3,9,BONE_S);ps(C.thigh_l,2,8,ROT1);
pn(C.shin_l,-1,3,ROT2);pn(C.forearm_r,-6,12,ROT1);ps(C.forearm_l,6,11,ROT2);
// --- 腰裙：稻秆麻布 ---
clothFace(C.loin_front,'north');clothFace(C.loin_front,'south');
clothFace(C.loin_side_l,'west');clothFace(C.loin_side_r,'east');
// --- 背脊骨突：米白椎骨 ---
for(const n of ['spine1','spine2','spine3'])for(const f of ['north','south','east','west','up','down'])rect(C[n].f[f],BONE);
ps(C.spine1,0,16,BONE_S);ps(C.spine3,0,21,BONE_S);
// --- 猪蹄 ---
for(const n of ['hoof_r','hoof_l']){const c=C[n];for(const f of ['north','south','east','west','up','down'])hoof(c,f);}
// --- 尾巴 ---
for(const n of ['tail_base','tail_curl']){const c=C[n];skin(c,'north',{blobs:0});skin(c,'south',{blobs:0});skin(c,'east',{blobs:0});skin(c,'west',{blobs:0});}
for(const f of ['north','south','east','west','up','down'])rect(C.tail_tip.f[f],ROT_D);
// --- 金剑（金稻穗色） ---
const b=C.sword_blade;
for(const f of ['north','south']){const r=b.f[f];const h=Math.abs(r[3]-r[1]);const pat=[GOLD,GOLD_L,GOLD,GOLD,GOLD_L,GOLD,GOLD,GOLD_S];
 for(let ly=0;ly<h;ly++)at(r,0,ly,pat[ly]||GOLD);}
rect(b.f.east,GOLD_S);rect(b.f.west,GOLD_S);
for(const f of ['north','south','east','west','up','down'])rect(C.sword_tip.f[f],GOLD_L);
for(const f of ['north','south']){const r=C.sword_guard.f[f];at(r,0,0,GOLD_L);at(r,1,0,GOLD);}
rect(C.sword_guard.f.east,GOLD_S);rect(C.sword_guard.f.west,GOLD_S);
rect(C.sword_grip.f.north,GRIP);rect(C.sword_grip.f.south,GRIP);
rect(C.sword_grip.f.east,GRIP_S);rect(C.sword_grip.f.west,GRIP_S);

// ---------------- 输出 PNG ----------------
function crc32(buf){let t,crc=0xFFFFFFFF;for(let i=0;i<buf.length;i++){t=(crc^buf[i])&0xFF;for(let j=0;j<8;j++)t=t&1?0xEDB88320^(t>>>1):t>>>1;crc=(crc>>>8)^t;}return(crc^0xFFFFFFFF)>>>0;}
function chunk(type,data){const len=Buffer.alloc(4);len.writeUInt32BE(data.length);const tb=Buffer.from(type,'ascii');const cd=Buffer.concat([tb,data]);const crc=Buffer.alloc(4);crc.writeUInt32BE(crc32(cd));return Buffer.concat([len,cd,crc]);}
const raw=Buffer.alloc(H*(W*4+1));
for(let v=0;v<H;v++){raw[v*(W*4+1)]=0;img.copy(raw,v*(W*4+1)+1,v*W*4,(v+1)*W*4);}
const png=Buffer.concat([Buffer.from([0x89,0x50,0x4E,0x47,0x0D,0x0A,0x1A,0x0A]),
 chunk('IHDR',(()=>{const b=Buffer.alloc(13);b.writeUInt32BE(W,0);b.writeUInt32BE(H,4);b[8]=8;b[9]=6;return b;})()),
 chunk('IDAT',zlib.deflateSync(raw)),chunk('IEND',Buffer.alloc(0))]);
fs.mkdirSync(__dirname,{recursive:true});
fs.writeFileSync(path.join(__dirname,'mihengzhushi.png'),png);
// UV 表（写回 BB 用）
const uvout={};for(const c of Cb){uvout[c.n]={};for(const f of ['north','south','east','west','up','down'])uvout[c.n][f]=c.f[f];}
fs.writeFileSync(path.join(__dirname,'uv.json'),JSON.stringify(uvout));
console.log('OK png+uv written, layoutHeight='+(y+sh));
