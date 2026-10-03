// 米哞牛纹理生成器：box UV 打包 + 大米主题像素绘制
const fs = require('fs');
const zlib = require('zlib');
const path = require('path');

const W = 128, H = 128;

// ---------- 立方体尺寸表 [x, y, z] ----------
const dims = {
  body_main: [12, 9, 18], body_barrel: [13, 5, 14], body_spine: [11, 1.5, 16],
  brisket: [10, 3, 6], udder: [5, 3.5, 5],
  teat_fl: [1, 1.5, 1], teat_fr: [1, 1.5, 1], teat_bl: [1, 1.5, 1], teat_br: [1, 1.5, 1],
  neck: [8, 5.5, 6],
  cranium: [8, 8, 6], muzzle: [7, 4, 3],
  horn_base_r: [1.5, 2, 2], horn_base_l: [1.5, 2, 2],
  horn_tip_r: [1.5, 2.5, 1.25], horn_tip_l: [1.5, 2.5, 1.25],
  ear_r: [2.5, 1, 3.5], ear_l: [2.5, 1, 3.5],
  thigh_r: [4.25, 6.5, 4], thigh_l: [4.25, 6.5, 4],
  shin_br: [3.25, 4.5, 3], shin_bl: [3.25, 4.5, 3],
  hoof_br: [3.75, 2.5, 3.75], hoof_bl: [3.75, 2.5, 3.75],
  shoulder_r: [3.5, 6.5, 4], shoulder_l: [3.5, 6.5, 4],
  shin_fr: [3, 4.5, 3], shin_fl: [3, 4.5, 3],
  hoof_fr: [3.75, 2.5, 3.75], hoof_fl: [3.75, 2.5, 3.75],
  tail_base: [1.5, 3, 1.5], tail_stem: [1, 6, 1], tail_tuft: [2.5, 3.5, 2],
};

// ---------- 天际线装箱（按高度排序，换货架） ----------
const netOf = ([x, y, z]) => [Math.ceil(2 * (x + z)), Math.ceil(y + z)];
const items = Object.entries(dims).map(([name, d]) => {
  const [nw, nh] = netOf(d);
  return { name, d: d, nw, nh };
}).sort((a, b) => b.nh - a.nh || b.nw - a.nw);

const offset = {};
let shelfY = 0, shelfX = 0, shelfH = 0;
for (const it of items) {
  if (shelfX + it.nw > W) {
    shelfY += shelfH; shelfX = 0; shelfH = 0;
  }
  offset[it.name] = [shelfX, shelfY];
  shelfX += it.nw;
  shelfH = Math.max(shelfH, it.nh);
}
const totalH = shelfY + shelfH;
if (totalH > H) { console.error('PACK OVERFLOW', totalH); process.exit(1); }

// ---------- box UV 六面矩形（标准 net 布局，未镜像） ----------
// N:[u+z,v+z]x(x,y) E:[u+z+x,v+z]x(z,y) W:[u,v+z]x(z,y) S:[u+2z+x,v+z]x(x,y)
// up 区域:[u+z,v]x(x,z) down区域:[u+z+x,v]x(x,z)  (存储端点反转，区域相同)
function rects(name) {
  const [x, y, z] = dims[name];
  const [u, v] = offset[name];
  return {
    n: [u + z, v + z, x, y],
    e: [u + z + x, v + z, z, y],
    w: [u, v + z, z, y],
    s: [u + 2 * z + x, v + z, x, y],
    u: [u + z, v, x, z],
    d: [u + z + x, v, x, z],
  };
}

// ---------- 画布 ----------
const buf = Buffer.alloc(W * H * 4, 0);
function idx(px, py) { return ((py << 7) + px) * 4; }
function setPx(px, py, c, a = 255) {
  px = px | 0; py = py | 0;
  if (px < 0 || py < 0 || px >= W || py >= H) return;
  const i = idx(px, py);
  buf[i] = c[0]; buf[i + 1] = c[1]; buf[i + 2] = c[2]; buf[i + 3] = a;
}
function getPx(px, py) {
  const i = idx(px | 0, py | 0);
  return [buf[i], buf[i + 1], buf[i + 2]];
}
function fill(rc, c) {
  const [x, y, w, h] = rc;
  const x0 = Math.floor(x), y0 = Math.floor(y), x1 = Math.ceil(x + w), y1 = Math.ceil(y + h);
  for (let py = y0; py < y1; py++) for (let px = x0; px < x1; px++) setPx(px, py, c);
}
function rect(rc, c) { fill(rc, c); }
// 模型坐标 -> 侧面像素 (dir: 'e' 或 'w')
function sideCol(rc, dir, z, zfrom, zto) {
  const x0 = rc[0];
  return dir === 'w' ? x0 + (z - zfrom) : x0 + (zto - z);
}
function rowY(rc, y, yto) { return rc[1] + (yto - y); }
// 椭圆斑块（带锯齿边和深色边缘）
let _seed = 1234567;
function rnd() { _seed = (_seed * 1664525 + 1013904223) >>> 0; return _seed / 4294967296; }
function blob(rc, cz0, cz1, cy0, cy1, dir, zfrom, zto, yfrom, yto, col, edge) {
  const czc = (cz0 + cz1) / 2, cyc = (cy0 + cy1) / 2, rz = (cz1 - cz0) / 2, ry = (cy1 - cy0) / 2;
  for (let mz = Math.floor(cz0); mz < cz1; mz++) {
    for (let my = Math.floor(cy0); my < cy1; my++) {
      const dz = (mz + 0.5 - czc) / rz, dy = (my + 0.5 - cyc) / ry;
      const jitter = 0.92 + rnd() * 0.16;
      if (dz * dz + dy * dy <= jitter) {
        const cx = sideCol(rc, dir, mz + 0.5, zfrom, zto) | 0;
        const cy = (rc[1] + (yto - (my + 0.5))) | 0;
        const onEdge = dz * dz + dy * dy > 0.72;
        setPx(cx, cy, onEdge && edge ? edge : col);
      }
    }
  }
}
// 正面/背面斑块（模型 x/y）
function blobXY(rc, xfrom, xto, yfrom, yto, cx0, cx1, cy0, cy1, col, edge) {
  const ccx = (cx0 + cx1) / 2, ccy = (cy0 + cy1) / 2, rx = (cx1 - cx0) / 2, ry = (cy1 - cy0) / 2;
  for (let mx = Math.floor(cx0); mx < cx1; mx++) {
    for (let my = Math.floor(cy0); my < cy1; my++) {
      const ddx = (mx + 0.5 - ccx) / rx, ddy = (my + 0.5 - ccy) / ry;
      if (ddx * ddx + ddy * ddy <= 0.95 + rnd() * 0.12) {
        const px = (rc[0] + (mx - xfrom)) | 0;
        const py = (rc[1] + (yto - (my + 0.5))) | 0;
        const onE = ddx * ddx + ddy * ddy > 0.72;
        setPx(px, py, onE && edge ? edge : col);
      }
    }
  }
}
// 稻粒：单像素暖色高光，稀疏点缀，只落在米白区域（无暗部，避免显脏）
function grains(rc, n, baseHint) {
  const [x, y, w, h] = rc;
  let placed = 0, guard = 0;
  while (placed < n && guard++ < n * 60) {
    const gx = x + rnd() * w, gy = y + 0.25 + rnd() * (h - 0.5);
    const px = gx | 0, py = gy | 0;
    const cur = getPx(px, py);
    if (cur[0] < 238 || cur[2] > cur[0] + 2 || cur[0] - cur[1] > 16) continue;
    setPx(px, py, [253, 249, 235]);
    placed++;
  }
}
function dot(rc, fx, fy, c) { setPx(rc[0] + fx, rc[1] + fy, c); }
function hline(rc, fx0, fx1, fy, c) { for (let i = fx0; i <= fx1; i++) dot(rc, i, fy, c); }
function vline(rc, fx, fy0, fy1, c) { for (let i = fy0; i <= fy1; i++) dot(rc, fx, i, c); }

// ---------- 调色板 ----------
const C = {
  white: [245, 240, 224], whiteTop: [251, 248, 236], whiteBelly: [240, 233, 210],
  whiteDown: [237, 230, 210],
  patch: [222, 202, 150], patchEdge: [203, 177, 122],
  pink: [238, 189, 174], pinkLight: [245, 205, 191], pinkDark: [213, 154, 136],
  nostril: [122, 78, 70],
  udder: [240, 195, 178], udderDark: [221, 164, 144], teat: [229, 169, 151],
  horn: [235, 220, 187], hornBand: [210, 192, 152], hornTip: [219, 202, 160],
  hoof: [78, 63, 54], hoofRim: [110, 89, 74], hoofDark: [56, 45, 39],
  green: [157, 184, 123], greenDark: [133, 164, 99],
  tuft: [94, 133, 67], tuftLight: [120, 160, 85], tuftDark: [72, 107, 52],
  black: [46, 36, 32], eyeWhite: [255, 255, 255],
  innerEar: [233, 173, 160],
};

const R = {};
for (const name of Object.keys(dims)) R[name] = rects(name);

function paintSideFur(name, xf, xt, yf, yt, zf, zt, grainN) {
  for (const dir of ['e', 'w']) {
    const rc = R[name][dir];
    fill(rc, C.white);
    // 顶亮底暗
    for (let px = rc[0] | 0; px < Math.ceil(rc[0] + rc[2]); px++) {
      setPx(px, rc[1] | 0, C.whiteTop);
      setPx(px, Math.ceil(rc[1] + rc[4] || rc[3]) - 1, C.whiteBelly);
    }
    const bh = Math.ceil(rc[3]);
    for (let px = rc[0] | 0; px < Math.ceil(rc[0] + rc[2]); px++)
      setPx(px, (rc[1] + bh - 1) | 0, C.whiteBelly);
    if (grainN) grains(rc, grainN);
  }
}

// ---------- 躯干 ----------
(function body() {
  // body_main
  const b = R.body_main;
  for (const k of ['n', 'e', 'w', 's', 'u', 'd']) fill(b[k], C.white);
  // 侧面
  for (const dir of ['e', 'w']) {
    const rc = b[dir]; // z -8..10, y 13..22
    const bh = Math.ceil(rc[3]);
    for (let px = rc[0] | 0; px < Math.ceil(rc[0] + rc[2]); px++) {
      setPx(px, rc[1] | 0, C.whiteTop);
      setPx(px, (rc[1] + bh - 1) | 0, C.whiteBelly);
    }
    blob(rc, -6.5, -2, 16.5, 21, dir, -8, 10, 13, 22, C.patch, C.patchEdge);
    blob(rc, 2.5, 8, 13.8, 17.5, dir, -8, 10, 13, 22, C.patch, C.patchEdge);
    grains(rc, 12);
  }
  // 臀部 S (z=+10 端面)
  blobXY(b.s, -6, 6, 13, 22, -2.5, 2.5, 16, 20, C.patch, C.patchEdge);
  grains(b.s, 6);
  // 胸前 N 被颈/胸遮挡，简单处理
  grains(b.n, 5);
  // 背部 up：米黄色背脊线
  const up = b.u;
  for (let py = up[1] | 0; py < Math.ceil(up[1] + up[3]); py++)
    for (let px = up[0] | 0; px < Math.ceil(up[0] + up[2]); px++) {
      const lx = px - up[0];
      if (lx >= 4 && lx <= 7) setPx(px, py, C.patch);
    }
  grains(up, 10);
  fill(b.d, C.whiteDown);

  // body_barrel（腹部）
  const br = R.body_barrel; // z -6..8 y14.5..19.5
  for (const k of ['n', 'e', 'w', 's', 'u', 'd']) fill(br[k], C.white);
  for (const dir of ['e', 'w']) {
    const rc = br[dir];
    const bh = Math.ceil(rc[3]);
    for (let px = rc[0] | 0; px < Math.ceil(rc[0] + rc[2]); px++) {
      setPx(px, (rc[1] + bh - 1) | 0, C.whiteBelly);
    }
    blob(rc, -4, -0.5, 16, 19.2, dir, -6, 8, 14.5, 19.5, C.patch, C.patchEdge);
    grains(rc, 4);
  }
  fill(br.u, C.whiteTop); fill(br.d, C.whiteDown);

  // body_spine 背脊条
  const sp = R.body_spine;
  for (const k of ['n', 'e', 'w', 's', 'u', 'd']) fill(sp[k], C.white);
  fill(sp.u, C.patch);
  grains(sp.u, 7);

  // brisket 前胸
  const bk = R.brisket;
  for (const k of ['n', 'e', 'w', 's', 'u', 'd']) fill(bk[k], C.white);
  fill(bk.n, C.whiteTop); grains(bk.n, 3);

  // neck 颈
  const nk = R.neck;
  for (const k of ['n', 'e', 'w', 's', 'u', 'd']) fill(nk[k], C.white);
  for (const dir of ['e', 'w']) {
    const rc = nk[dir]; // z -11..-5, y14.5..20
    blob(rc, -11, -8, 17, 20, dir, -11, -5, 14.5, 20, C.patch, C.patchEdge);
    grains(rc, 3);
  }

  // udder 乳房
  const ud = R.udder;
  for (const k of ['n', 'e', 'w', 's']) {
    fill(ud[k], C.udder);
    const rc = ud[k], bh = Math.ceil(rc[3]);
    for (let px = rc[0] | 0; px < Math.ceil(rc[0] + rc[2]); px++)
      setPx(px, (rc[1] + bh - 1) | 0, C.udderDark);
  }
  fill(ud.u, C.udder); fill(ud.d, C.pinkLight);
  for (const t of ['teat_fl', 'teat_fr', 'teat_bl', 'teat_br']) {
    const tc = R[t];
    for (const k of ['n', 'e', 'w', 's', 'u', 'd']) fill(tc[k], C.teat);
    for (const k of ['n', 'e', 'w', 's']) {
      const rc = tc[k];
      setPx((rc[0] + 0.01) | 0, Math.ceil(rc[1] + rc[3]) - 1, C.udderDark);
      setPx(Math.ceil(rc[0] + rc[2]) - 1, Math.ceil(rc[1] + rc[3]) - 1, C.udderDark);
    }
  }
})();

// ---------- 头部 ----------
(function head() {
  const cr = R.cranium; // x -4..4, y16..24, z -13..-7
  for (const k of ['n', 'e', 'w', 's', 'u', 'd']) fill(cr[k], C.white);
  // N 脸
  const n = cr.n;
  // 额头米色斑块 (y22..24 两行 + 第三行两侧角，给眼睛留位)
  for (let fy = 0; fy <= 1; fy++) hline(n, 0, 7, fy, C.patch);
  hline(n, 0, 1, 2, C.patch); hline(n, 6, 7, 2, C.patch);
  dot(n, 1, 2, C.patchEdge); dot(n, 6, 2, C.patchEdge);
  // 眼睛：白高光 row2 + 黑眼仁 row3（row3 高于 muzzle 顶 y20，不会被挡）
  dot(n, 2, 2, C.eyeWhite); dot(n, 2, 3, C.black);
  dot(n, 6, 2, C.eyeWhite); dot(n, 6, 3, C.black);
  // 脸侧麦粒
  grains(n, 3);
  // 侧面眼
  for (const dir of ['e', 'w']) {
    const rc = cr[dir];
    for (let fy = 0; fy <= 1; fy++) for (let c2 = 0; c2 < 3; c2++)
      setPx((rc[0] + (dir === 'e' ? rc[2] - 3 + c2 : c2)) | 0, rc[1] + fy, C.patch);
    const frontCol = dir === 'e' ? Math.ceil(rc[0] + rc[2]) - 1 : rc[0] | 0;
    setPx(frontCol, rc[1] + 2, C.eyeWhite);
    setPx(frontCol, rc[1] + 3, C.black);
    grains(rc, 3);
  }
  fill(cr.u, C.patch); grains(cr.u, 3);

  // muzzle 口鼻 x -3.5..3.5 y16..20 z -15..-12
  const mz = R.muzzle;
  for (const k of ['n', 'e', 'w', 's', 'u', 'd']) fill(mz[k], C.pink);
  fill(mz.n, C.pink);
  hline(mz.n, 0, 6, 0, C.pinkLight);
  hline(mz.n, 0, 6, 3, C.pinkDark);
  dot(mz.n, 1, 3, C.nostril); dot(mz.n, 5, 3, C.nostril);
  dot(mz.n, 3, 3, C.pinkDark); // 人中
  dot(mz.n, 3, 2, C.pinkDark);
  // 顶面是毛
  fill(mz.u, C.white); grains(mz.u, 3);
  // 侧面：靠后为毛，靠前粉
  for (const dir of ['e', 'w']) {
    const rc = mz[dir];
    const wpx = Math.ceil(rc[2]);
    for (let c2 = 0; c2 < wpx; c2++) {
      const isFront = dir === 'e' ? c2 >= wpx - 2 : c2 <= 1;
      if (!isFront) for (let r2 = 0; r2 < rc[3]; r2++)
        setPx((rc[0] + c2) | 0, (rc[1] + r2) | 0, C.white);
    }
  }

  // 牛角
  for (const hb of ['horn_base_r', 'horn_base_l']) {
    const rc = R[hb];
    for (const k of ['n', 'e', 'w', 's', 'u', 'd']) fill(rc[k], C.horn);
    for (const k of ['n', 's']) { hline(rc[k], 0, Math.ceil(rc[k][2]) - 1, 1, C.hornBand); }
    fill(rc.u, C.hornBand);
  }
  for (const ht of ['horn_tip_r', 'horn_tip_l']) {
    const rc = R[ht];
    for (const k of ['n', 'e', 'w', 's', 'u', 'd']) fill(rc[k], C.horn);
    for (const k of ['n', 'e', 'w', 's']) {
      const r2 = rc[k];
      setPx(r2[0] | 0, r2[1] | 0, C.hornTip);
      if (r2[2] > 1.2) setPx(Math.ceil(r2[0] + r2[2]) - 1, r2[1] | 0, C.hornTip);
    }
    fill(rc.u, C.hornTip);
  }

  // 耳朵：外侧白毛，内侧粉
  for (const ear of ['ear_r', 'ear_l']) {
    const rc = R[ear];
    for (const k of ['n', 'e', 'w', 's', 'u', 'd']) fill(rc[k], C.white);
    fill(rc.u, C.innerEar);
    fill(rc.d, C.white);
  }
})();

// ---------- 四肢 ----------
(function legs() {
  const legPairs = [
    ['thigh_r', 5, 9, 6.5, 13], ['thigh_l', 5, 9, 6.5, 13],
    ['shoulder_r', -7, -3, 6.5, 13], ['shoulder_l', -7, -3, 6.5, 13],
  ];
  for (const [name, zf, zt, yf, yt] of legPairs) {
    const rc = R[name];
    for (const k of ['n', 'e', 'w', 's', 'u', 'd']) fill(rc[k], C.white);
    for (const dir of ['e', 'w']) {
      const s = rc[dir];
      blob(s, zf + 0.7, zt - 0.7, 9.5, 12.8, dir, zf, zt, yf, yt, C.patch, C.patchEdge);
      grains(s, 2);
    }
    fill(rc.d, C.whiteDown);
  }
  for (const sh of ['shin_br', 'shin_bl', 'shin_fr', 'shin_fl']) {
    const rc = R[sh];
    for (const k of ['n', 'e', 'w', 's', 'u', 'd']) fill(rc[k], C.white);
    grains(rc.e, 1); grains(rc.w, 1);
    fill(rc.d, C.whiteDown);
  }
  for (const hf of ['hoof_br', 'hoof_bl', 'hoof_fr', 'hoof_fl']) {
    const rc = R[hf];
    for (const k of ['n', 'e', 'w', 's', 'u', 'd']) fill(rc[k], C.hoof);
    for (const k of ['n', 'e', 'w', 's']) {
      const s = rc[k];
      hline(s, 0, Math.ceil(s[2]) - 1, 0, C.hoofRim);
      hline(s, 0, Math.ceil(s[2]) - 1, Math.ceil(s[3]) - 1, C.hoofDark);
    }
  }
})();

// ---------- 尾巴（稻秆+稻叶尾帚） ----------
(function tail() {
  const tb = R.tail_base;
  for (const k of ['n', 'e', 'w', 's', 'u', 'd']) fill(tb[k], C.white);
  grains(tb.e, 1); grains(tb.w, 1);
  const ts = R.tail_stem; // 1 x 6 x 1 稻秆
  for (const k of ['n', 'e', 'w', 's', 'u', 'd']) fill(ts[k], C.greenDark);
  for (const k of ['n', 'e', 'w', 's']) fill(ts[k], C.green);
  fill(ts.d, C.greenDark);
  const tt = R.tail_tuft;
  for (const k of ['n', 'e', 'w', 's', 'u', 'd']) fill(tt[k], C.tuft);
  for (const k of ['n', 's']) {
    const rc = tt[k];
    for (let py = rc[1] | 0; py < Math.ceil(rc[1] + rc[3]); py++) {
      setPx(rc[0] | 0, py, C.tuftDark);
      setPx(Math.ceil(rc[0] + rc[2]) - 1, py, C.tuftDark);
    }
    vline(tt[k], 1, 0, 1, C.tuftLight);
  }
  for (const k of ['e', 'w']) {
    const rc = tt[k];
    for (let py = rc[1] | 0; py < Math.ceil(rc[1] + rc[3]); py++)
      setPx(rc[0] | 0, py, C.tuftDark);
  }
  fill(tt.d, C.tuftDark);
})();

// ---------- PNG 编码 ----------
function crc32(buf) {
  let c, crc = 0xffffffff;
  for (let i = 0; i < buf.length; i++) {
    c = (crc ^ buf[i]) & 0xff;
    for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
    crc = (crc >>> 8) ^ c;
  }
  return (crc ^ 0xffffffff) >>> 0;
}
function chunk(type, data) {
  const len = Buffer.alloc(4); len.writeUInt32BE(data.length, 0);
  const td = Buffer.concat([Buffer.from(type, 'ascii'), data]);
  const crc = Buffer.alloc(4); crc.writeUInt32BE(crc32(td), 0);
  return Buffer.concat([len, td, crc]);
}
const raw = Buffer.alloc((W * 4 + 1) * H);
for (let y = 0; y < H; y++) {
  raw[y * (W * 4 + 1)] = 0;
  buf.copy(raw, y * (W * 4 + 1) + 1, y * W * 4, (y + 1) * W * 4);
}
const ihdr = Buffer.alloc(13);
ihdr.writeUInt32BE(W, 0); ihdr.writeUInt32BE(H, 4);
ihdr[8] = 8; ihdr[9] = 6; // 8bit RGBA
const png = Buffer.concat([
  Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
  chunk('IHDR', ihdr),
  chunk('IDAT', zlib.deflateSync(raw, { level: 9 })),
  chunk('IEND', Buffer.alloc(0)),
]);
const outDir = __dirname;
fs.writeFileSync(path.join(outDir, 'mimouniu.png'), png);

// ---------- 导出 Blockbench 应用计划（offset + 六面 uv） ----------
const plan = {};
for (const name of Object.keys(dims)) {
  plan[name] = { off: offset[name], faces: R[name] };
}
fs.writeFileSync(path.join(outDir, 'plan.json'), JSON.stringify(plan));
console.log('OK packed height=' + totalH + ' cubes=' + Object.keys(dims).length);
