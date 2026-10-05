// 米魂 mihun 大米主题贴图生成（零依赖 PNG），128x128 box-UV
const fs=require('fs'),zlib=require('zlib'),path=require('path');
const W=128,H=128;
// [name, from[3], to[3]]
const BOXES=[
['body_core',[-9,8,-9],[9,22,9]],
['dome_1',[-7,22,-7],[7,25,7]],
['dome_2',[-4.5,25,-4.5],[4.5,27,4.5]],
['dome_3',[-2,27,-2],[2,28.5,2]],
['flap_n_l',[-8.5,5,-10.5],[-1.5,8,-8.5]],['flap_n_r',[1.5,5,-10.5],[8.5,8,-8.5]],
['flap_s_l',[-8.5,5,8.5],[-1.5,8,10.5]],['flap_s_r',[1.5,5,8.5],[8.5,8,10.5]],
['flap_w_f',[-10.5,5,-8.5],[-8.5,8,-1.5]],['flap_w_b',[-10.5,5,1.5],[-8.5,8,8.5]],
['flap_e_f',[8.5,5,-8.5],[10.5,8,-1.5]],['flap_e_b',[8.5,5,1.5],[10.5,8,8.5]],
['corner_nw',[-9.5,5.5,-9.5],[-6,8,-6]],['corner_ne',[6,5.5,-9.5],[9.5,8,-6]],
['corner_sw',[-9.5,5.5,6],[-6,8,9.5]],['corner_se',[6,5.5,6],[9.5,8,9.5]],
['eye_l',[-6.2,15,-9.7],[-2.8,18.2,-8.9]],['eye_r',[2.8,15,-9.7],[6.2,18.2,-8.9]],
['brow_l',[-6.6,18.6,-9.9],[-2.4,19.8,-8.7]],['brow_r',[2.4,18.6,-9.9],[6.6,19.8,-8.7]],
['mouth',[-2.4,11.2,-9.7],[2.4,14.2,-8.9]],
['tear_l',[-5.1,12.8,-9.9],[-3.9,15,-9.2]],['tear_r',[3.9,12.8,-9.9],[5.1,15,-9.2]],
['wisp_l_1',[-7.7,25.8,-2.7],[-6.3,28,-1.3]],['wisp_l_2',[-8.9,27.9,-2.7],[-7.3,29.7,-1.3]],
['wisp_r_1',[6.3,25.8,-2.7],[7.7,28,-1.3]],['wisp_r_2',[7.3,27.9,-2.7],[8.9,29.7,-1.3]],
['t_nw_1',[-6,4,-6],[-4,8,-4]],['t_nw_2',[-5.7,0.4,-5.7],[-4.3,4.4,-4.3]],['t_nw_3',[-5.45,-2.6,-5.45],[-4.55,0.6,-4.55]],
['t_n_1',[-1,4,-6],[1,8,-4]],['t_n_2',[-0.7,0.4,-5.7],[0.7,4.4,-4.3]],['t_n_3',[-0.45,-2.6,-5.45],[0.45,0.6,-4.55]],
['t_ne_1',[4,4,-6],[6,8,-4]],['t_ne_2',[4.3,0.4,-5.7],[5.7,4.4,-4.3]],['t_ne_3',[4.55,-2.6,-5.45],[5.45,0.6,-4.55]],
['t_w_1',[-6,4,-1],[-4,8,1]],['t_w_2',[-5.7,0.4,-0.7],[-4.3,4.4,0.7]],['t_w_3',[-5.45,-2.6,-0.45],[-4.55,0.6,0.45]],
['t_c_1',[-1,4,-1],[1,8,1]],['t_c_2',[-0.7,0.4,-0.7],[0.7,4.4,0.7]],['t_c_3',[-0.45,-2.6,-0.45],[0.45,0.6,0.45]],
['t_e_1',[4,4,-1],[6,8,1]],['t_e_2',[4.3,0.4,-0.7],[5.7,4.4,0.7]],['t_e_3',[4.55,-2.6,-0.45],[5.45,0.6,0.45]],
['t_sw_1',[-6,4,4],[-4,8,6]],['t_sw_2',[-5.7,0.4,4.3],[-4.3,4.4,5.7]],['t_sw_3',[-5.45,-2.6,4.55],[-4.55,0.6,5.45]],
['t_s_1',[-1,4,4],[1,8,6]],['t_s_2',[-0.7,0.4,4.3],[0.7,4.4,5.7]],['t_s_3',[-0.45,-2.6,4.55],[0.45,0.6,5.45]],
['t_se_1',[4,4,4],[6,8,6]],['t_se_2',[4.3,0.4,4.3],[5.7,4.4,5.7]],['t_se_3',[4.55,-2.6,4.55],[5.45,0.6,5.45]],
];
const rz=v=>Math.floor(Math.round(v*1000)/1000);
const B=BOXES.map(([n,f,t])=>({n,w:Math.max(1,Math.round(rz(t[0]-f[0]))),h:Math.max(1,Math.round(rz(t[1]-f[1]))),d:Math.max(1,Math.round(rz(t[2]-f[2])))}));
// 货架装箱
const PAD=1;const items=B.map(b=>({b,nw:2*(b.d+b.w),nh:b.d+b.h})).sort((a,c)=>c.nh-a.nh);
const shelves=[];const pos={};
for(const it of items){let placed=false;
 for(const s of shelves){if(s.used+it.nw+PAD<=W){pos[it.b.n]=[s.used,s.y];s.used+=it.nw+PAD;placed=true;break;}}
 if(!placed){const y=shelves.length?shelves[shelves.length-1].y+shelves[shelves.length-1].h+PAD:0;shelves.push({y,h:it.nh,used:it.nw+PAD});pos[it.b.n]=[0,y];}}
const layoutH=shelves[shelves.length-1].y+shelves[shelves.length-1].h;
// 画布
const px=Buffer.alloc(W*H*4);
function at(x,y,c){if(x<0||y<0||x>=W||y>=H)return;const i=(y*W+x)*4;px[i]=c[0];px[i+1]=c[1];px[i+2]=c[2];px[i+3]=255;}
function rect(r,c){for(let y=r[1];y<r[3];y++)for(let x=r[0];x<r[2];x++)at(x,y,c);}
// 配色
const BASE=[237,233,220],LIGHT=[248,245,235],SHADE=[211,203,184],DEEP=[183,173,153],GRAIN=[252,249,239];
const WARM=[226,218,199],WARM_S=[196,185,162];
const SOY=[33,27,21],SOY2=[54,43,33];
const TEAR=[148,170,183],TEAR_L=[196,213,220],TEAR_S=[103,126,140];
const GOLD=[236,190,66],GOLD_L=[253,235,158],GOLD_S=[176,128,38];
function hsh(s){let h=2166136261;for(let i=0;i<s.length;i++){h^=s.charCodeAt(i);h=Math.imul(h,16777619);}return h>>>0;}
function rng(a){return function(){a|=0;a=a+0x6D2B79F5|0;let t=Math.imul(a^a>>>15,1|a);t=t+Math.imul(t^t>>>7,61|t)^t;return((t^t>>>14)>>>0)/4294967296;}}
// box net 六个面
function faces(U,V,w,h,d){return{north:[U+d,V+d,U+d+w,V+d+h],east:[U+d+w,V+d,U+d+w+d,V+d+h],south:[U+d+w+d,V+d,U+d+w+d+w,V+d+h],west:[U,V+d,U+d,V+d+h],up:[U+d,V,U+d+w,V+d],down:[U+d+w,V,U+d+w+w,V+d]};}
function ghost(r,name,face,o={}){const w=r[2]-r[0],h=r[3]-r[1];const base=o.warm?WARM:BASE,sh=o.warm?WARM_S:SHADE;const rnd=rng(hsh(name+face));
 for(let ly=0;ly<h;ly++)for(let lx=0;lx<w;lx++){let col=base,q=rnd();
  if(face==='up'){col=q<0.5?LIGHT:base;if(q>0.9)col=GRAIN;}
  else if(face==='down'){col=q<0.6?sh:DEEP;}
  else{if(ly===0&&q<0.45)col=LIGHT;else if(ly===h-1&&q<0.6)col=sh;else if(q<0.06)col=sh;else if(q<0.105)col=LIGHT;
   if(q>0.985)col=GRAIN;if(face==='east'||face==='west'){if(q<0.10)col=sh;}}
  at(r[0]+lx,r[1]+ly,col);}
 if(face==='up'){at(r[0]+Math.max(0,(w/2|0)-1),r[1]+(h/2|0),GRAIN);}
}
function darkFill(r,c=SOY){rect(r,c);const w=r[2]-r[0],h=r[3]-r[1];for(let lx=0;lx<w;lx++){at(r[0]+lx,r[1],SOY2);at(r[0]+lx,r[3]-1,SOY);}}
function tearFace(r,name,face){const w=r[2]-r[0],h=r[3]-r[1];const rnd=rng(hsh(name+face));
 for(let ly=0;ly<h;ly++)for(let lx=0;lx<w;lx++){let col=TEAR,q=rnd();
  if(ly===0)col=TEAR_L;else if(ly===h-1)col=TEAR_S;else if(q<0.3)col=TEAR_L;
  at(r[0]+lx,r[1]+ly,col);}}
function goldFace(r,name,face){const w=r[2]-r[0],h=r[3]-r[1];const rnd=rng(hsh(name+face));
 for(let ly=0;ly<h;ly++)for(let lx=0;lx<w;lx++){let col=GOLD,q=rnd();
  if(ly===0)col=GOLD_L;else if(ly===h-1)col=GOLD_S;else if(q<0.25)col=GOLD_L;else if(q>0.85)col=GOLD_S;
  at(r[0]+lx,r[1]+ly,col);}}
for(const b of B){const[U,V]=pos[b.n];const{w,h,d}=b;const F=faces(U,V,w,h,d);const n=b.n;
 if(/^eye_/.test(n)){for(const f of['north','south'])darkFill(F[f]);for(const f of['east','west','up','down'])darkFill(F[f],SOY2);
  for(const r of[F.north,F.south]){at(r[0]+1,r[1]+1,GRAIN);}
 }else if(/^brow_/.test(n)){for(const f in F)darkFill(F[f]);}
 else if(n==='mouth'){for(const f in F)darkFill(F[f],SOY2);const r=F.north;for(let lx=0;lx<r[2]-r[0];lx++)at(r[0]+lx,r[3]-1,SOY);at(r[0]+1,r[1]+1,SOY);}
 else if(/^tear_/.test(n)){for(const f in F)tearFace(F[f],n,f);}
 else if(/wisp_._2$/.test(n)){for(const f in F)goldFace(F[f],n,f);}
 else if(/wisp_._1$/.test(n)){for(const f in F){ghost(F[f],n,f);const r=F[f];if((f==='up'||f==='north')){for(let lx=0;lx<r[2]-r[0];lx++){if((lx+ (f==='up'?1:0))%2===0)at(r[0]+lx,r[1],GOLD_L);}}}
 }else if(/_3$/.test(n)){for(const f in F)ghost(F[f],n,f,{warm:true});}
 else{for(const f in F)ghost(F[f],n,f);}
}
// 输出 PNG
function chunk(type,data){const len=Buffer.alloc(4);len.writeUInt32BE(data.length);const t=Buffer.from(type);const crcBuf=Buffer.concat([t,data]);let c=~0;for(let i=0;i<crcBuf.length;i++){c^=crcBuf[i];for(let k=0;k<8;k++)c=(c>>>1)^(0xEDB88320&-(c&1));}c=(~c)>>>0;const cb=Buffer.alloc(4);cb.writeUInt32BE(c);return Buffer.concat([len,t,data,cb]);}
const ihdr=Buffer.alloc(13);ihdr.writeUInt32BE(W,0);ihdr.writeUInt32BE(H,4);ihdr[8]=8;ihdr[9]=6;
const raw=Buffer.alloc((W*4+1)*H);for(let y=0;y<H;y++){raw[y*(W*4+1)]=0;px.copy(raw,y*(W*4+1)+1,y*W*4,(y+1)*W*4);}
const png=Buffer.concat([Buffer.from([137,80,78,71,13,10,26,10]),chunk('IHDR',ihdr),chunk('IDAT',zlib.deflateSync(raw,{level:9})),chunk('IEND',Buffer.alloc(0))]);
const out=path.join(__dirname,'mihun.png');fs.writeFileSync(out,png);
const uv={};for(const b of B)uv[b.n]=[pos[b.n][0],pos[b.n][1],b.w,b.h,b.d];
fs.writeFileSync(path.join(__dirname,'mihun_uv.json'),JSON.stringify(uv));
console.log('OK',png.length,'bytes layoutH='+layoutH,'boxes='+B.length);
