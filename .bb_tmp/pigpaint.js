// 米哼猪纹理生成器：box UV 打包 + 大米/米粉主题像素绘制
const fs = require('fs');
const zlib = require('zlib');
const path = require('path');

const W = 128, H = 128;

// ---------- 去重后的元素尺寸（左右腿/耳共用同一 UV 区） ----------
const dims = {
  body_main: [11, 8.5, 16], body_barrel: [12, 5.5, 13], body_spine: [9, 1.25, 14.5],
  rump: [10.5, 6.5, 5.5], brisket: [9.5, 4, 5], neck: [7.5, 4.5, 4],
  cranium: [8, 7, 7], cheek: [7, 3, 6.5], snout: [5, 4, 2.5], ear: [3, 1, 3.5],
  thigh: [4.5, 4.5, 4.5], shin: [3.5, 3.5, 3.5], hoof: [3.6, 2.5, 3.8],
  shoulder: [4, 4.5, 4.5],
  tail_base: [1.5, 2, 1.5], tail_stem: [1, 1.5, 1],
  curl_a: [1.5, 1, 1], curl_b: [1, 2, 1], curl_c: [2, 1, 1], curl_d: [1, 1, 1], curl_e: [1, 0.75, 1],
};

// ---------- 天际线装箱 ----------
const netOf = ([x, y, z]) => [Math.ceil(2 * (x + z)), Math.ceil(y + z)];
const items = Object.entries(dims).map(([name, d]) => {
  const [nw, nh] = netOf(d);
  return { name, d, nw, nh };
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

// ---------- box UV 六面矩形 ----------
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
function sideCol(rc, dir, z, zfrom) {
  const x0 = rc[0];
  return dir === 'w' ? x0 + (z - zfrom) : x0 + (rc[2] - (z - zfrom));
}
let _seed = 987654;
function rnd() { _seed = (_seed * 1664525 + 1013904223) >>> 0; return _seed / 4294967296; }
function blob(rc, cz0, cz1, cy0, cy1, dir, zfrom, yfrom, yto, col, edge) {
  const czc = (cz0 + cz1) / 2, cyc = (cy0 + cy1) / 2, rz = (cz1 - cz0) / 2, ry = (cy1 - cy0) / 2;
  for (let mz = Math.floor(cz0); mz < cz1; mz++) {
    for (let my = Math.floor(cy0); my < cy1; my++) {
      const dz = (mz + 0.5 - czc) / rz, dy = (my + 0.5 - cyc) / ry;
      const jitter = 0.92 + rnd() * 0.16;
      if (dz * dz + dy * dy <= jitter) {
        const cx = sideCol(rc, dir, mz + 0.5, zfrom) | 0;
        const cy = (rc[1] + (yto - (my + 0.5))) | 0;
        const onEdge = dz * dz + dy * dy > 0.72;
        setPx(cx, cy, onEdge && edge ? edge : col);
      }
    }
  }
}
function blobXY(rc, cx0, cx1, cy0, cy1, xfrom, xto, yfrom, yto, col, edge) {
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
function grains(rc, n) {
  const [x, y, w, h] = rc;
  let placed = 0, guard = 0;
  while (placed < n && guard++ < n * 60) {
    const gx = x + rnd() * w, gy = y + 0.25 + rnd() * Math.max(0, h - 0.5);
    const px = gx | 0, py = gy | 0;
    const cur = getPx(px, py);
    if (cur[0] < 238 || cur[2] > cur[0] + 2 || cur[0] - cur[1] > 16) continue;
    setPx(px, py, [253, 249, 235]);
    placed++;
  }
}
function dot(rc, fx, fy, c) { setPx(rc[0] + fx, rc[1] + fy, c); }
function hline(rc, fx0, fx1, fy, c) { for (let i = fx0; i <= fx1; i++) dot(rc, i, fy, c); }

// ---------- 调色板：米浆白 + 米粉腮红 + 稻黄 + 稻芽绿 ----------
const C = {
  white: [246, 239, 228], whiteTop: [252, 247, 238], belly: [234, 219, 203], down: [230, 214, 199],
  patch: [223, 203, 152], patchEdge: [203, 178, 122],
  blush: [241, 196, 184], blushEdge: [223, 170, 158],
  pink: [239, 188, 172], pinkLight: [246, 205, 190], pinkDark: [213, 153, 135],
  jowl: [243, 210, 198],
  nostril: [119, 75, 67], innerEar: [237, 177, 162],
  hoof: [74, 60, 52], hoofRim: [107, 87, 73], hoofDark: [52, 42, 37],
  green: [148, 178, 104], greenDark: [120, 152, 84], greenLight: [173, 200, 130],
  black: [44, 35, 31], eyeWhite: [255, 255, 255],
};

const R = {};
for (const name of Object.keys(dims)) R[name] = rects(name);

function whiteBase(name) {
  for (const k of ['n', 'e', 'w', 's', 'u', 'd']) fill(R[name][k], C.white);
}
function sideShade(name) {
  for (const dir of ['e', 'w']) {
    const rc = R[name][dir];
    const bh = Math.ceil(rc[3]);
    for (let px = rc[0] | 0; px < Math.ceil(rc[0] + rc[2]); px++) {
      setPx(px, rc[1] | 0, C.whiteTop);
      setPx(px, (rc[1] + bh - 1) | 0, C.belly);
    }
  }
}

// ---------- 躯干 ----------
(function body() {
  // body_main  x[-5.5,5.5] y[10,18.5] z[-7,9]
  whiteBase('body_main');
  sideShade('body_main');
  for (const dir of ['e', 'w']) {
    const rc = R.body_main[dir];
    blob(rc, -6.6, -5.2, 12.2, 15.2, dir, -7, 10, 18.5, C.blush, C.blushEdge);
    blob(rc, -6.6, -5.2, 16, 18, dir, -7, 10, 18.5, C.patch, C.patchEdge);
    grains(rc, 7);
  }
  fill(R.body_main.d, C.down);
  grains(R.body_main.u, 8);
  grains(R.body_main.n, 4);

  // body_barrel  x±6 y[11.5,17] z[-5,8]（圆肚，侧面主体）
  whiteBase('body_barrel');
  for (const dir of ['e', 'w']) {
    const rc = R.body_barrel[dir];
    const bh = Math.ceil(rc[3]);
    for (let px = rc[0] | 0; px < Math.ceil(rc[0] + rc[2]); px++) {
      setPx(px, rc[1] | 0, C.whiteTop);
      setPx(px, (rc[1] + bh - 1) | 0, C.belly);
    }
    blob(rc, -2.5, 1.5, 14.3, 16.7, dir, -5, 11.5, 17, C.patch, C.patchEdge);
    blob(rc, 3.2, 7.2, 12.1, 14.6, dir, -5, 11.5, 17, C.blush, C.blushEdge);
    grains(rc, 8);
  }
  fill(R.body_barrel.u, C.whiteTop);
  fill(R.body_barrel.d, C.down);

  // body_spine 背脊：稻黄色脊线
  whiteBase('body_spine');
  fill(R.body_spine.u, C.patch);
  grains(R.body_spine.u, 8);

  // rump 肥臀 y[11,17.5] z[3.5,9]
  whiteBase('rump');
  sideShade('rump');
  for (const dir of ['e', 'w']) {
    const rc = R.rump[dir];
    blob(rc, 5.6, 8.4, 15.2, 17.2, dir, 3.5, 11, 17.5, C.patch, C.patchEdge);
    blob(rc, 7.6, 9.6, 12.2, 14.8, dir, 3.5, 11, 17.5, C.blush, C.blushEdge);
    grains(rc, 3);
  }
  const s = R.rump.s;
  for (let px = s[0] | 0; px < Math.ceil(s[0] + s[2]); px++) setPx(px, s[1] | 0, C.whiteTop);
  blobXY(s, -2.4, 2.4, 13, 16.4, -5.25, 5.25, 11, 17.5, C.blush, C.blushEdge);
  blobXY(s, -1, 1, 16.2, 17.3, -5.25, 5.25, 11, 17.5, C.patch, C.patchEdge);
  grains(s, 5);
  fill(R.rump.d, C.down);
  grains(R.rump.u, 4);

  // brisket 前胸 y[10.5,14.5] z[-8,-3]
  whiteBase('brisket');
  sideShade('brisket');
  const bn = R.brisket.n;
  blobXY(bn, -1.6, 1.6, 11.6, 13.4, -4.75, 4.75, 10.5, 14.5, C.patch, C.patchEdge);
  grains(bn, 3);
  fill(R.brisket.d, C.down);

  // neck 颈 y[12.5,17] z[-9.5,-5.5]
  whiteBase('neck');
  sideShade('neck');
  for (const dir of ['e', 'w']) {
    const rc = R.neck[dir];
    blob(rc, -9.2, -7.2, 13.6, 16, dir, -9.5, 12.5, 17, C.blush, C.blushEdge);
    grains(rc, 2);
  }
})();

// ---------- 头部 ----------
(function head() {
  // cranium x[-4,4] y[14,21] z[-13,-6]
  whiteBase('cranium');
  const n = R.cranium.n;
  hline(n, 2, 5, 0, C.patch);
  dot(n, 2, 0, C.patchEdge); dot(n, 5, 0, C.patchEdge);
  dot(n, 1, 1, C.eyeWhite); dot(n, 1, 2, C.black);
  dot(n, 6, 1, C.eyeWhite); dot(n, 6, 2, C.black);
  dot(n, 0, 5, C.blush); dot(n, 0, 6, C.blushEdge);
  dot(n, 7, 5, C.blush); dot(n, 7, 6, C.blushEdge);
  grains(n, 3);
  for (const dir of ['e', 'w']) {
    const rc = R.cranium[dir];
    const frontCol = dir === 'e' ? Math.ceil(rc[0] + rc[2]) - 1 : rc[0] | 0;
    setPx(frontCol, rc[1] + 1, C.eyeWhite);
    setPx(frontCol, rc[1] + 2, C.black);
    blob(rc, -12.6, -10.2, 18.6, 20.6, dir, -13, 14, 21, C.patch, C.patchEdge);
    blob(rc, -12.7, -11.2, 15.2, 17.1, dir, -13, 14, 21, C.blush, C.blushEdge);
    grains(rc, 3);
  }
  fill(R.cranium.u, C.patch);
  grains(R.cranium.u, 3);

  // cheek 腮肉 y[12.5,15.5] z[-12.5,-6]
  whiteBase('cheek');
  for (const dir of ['e', 'w']) {
    const rc = R.cheek[dir];
    const bh = Math.ceil(rc[3]);
    for (let px = rc[0] | 0; px < Math.ceil(rc[0] + rc[2]); px++) {
      setPx(px, rc[1] | 0, C.whiteTop);
      setPx(px, (rc[1] + bh - 1) | 0, C.blushEdge);
    }
    blob(rc, -12.3, -6.8, 12.8, 15.1, dir, -12.5, 12.5, 15.5, C.jowl, C.blushEdge);
  }
  fill(R.cheek.d, C.pinkLight);

  // snout 鼻吻 y[14.5,18.5] z[-15.5,-13]
  whiteBase('snout');
  for (const k of ['n', 'e', 'w', 's', 'd']) fill(R.snout[k], C.pink);
  const sn = R.snout.n;
  hline(sn, 0, 4, 0, C.pinkLight);
  hline(sn, 0, 4, 3, C.pinkDark);
  dot(sn, 1, 2, C.nostril); dot(sn, 3, 2, C.nostril);
  dot(sn, 2, 1, C.pinkDark); dot(sn, 2, 2, C.pinkDark);
  fill(R.snout.u, C.white);
  grains(R.snout.u, 2);
  for (const dir of ['e', 'w']) {
    const rc = R.snout[dir];
    const wpx = Math.ceil(rc[2]);
    for (let c2 = 0; c2 < wpx; c2++) {
      const isFront = dir === 'e' ? c2 >= wpx - 2 : c2 <= 1;
      if (!isFront) for (let r2 = 0; r2 < rc[3]; r2++)
        setPx((rc[0] + c2) | 0, (rc[1] + r2) | 0, C.white);
    }
    const bh = Math.ceil(rc[3]);
    for (let px = rc[0] | 0; px < Math.ceil(rc[0] + rc[2]); px++)
      setPx(px, (rc[1] + bh - 1) | 0, C.pinkDark);
  }

  // ear 垂耳（左右共用）
  whiteBase('ear');
  fill(R.ear.u, C.innerEar);
  for (const k of ['n', 's']) {
    const rc = R.ear[k];
    for (let px = rc[0] | 0; px < Math.ceil(rc[0] + rc[2]); px++)
      setPx(px, Math.ceil(rc[1] + rc[3]) - 1, C.blushEdge);
  }
})();

// ---------- 四肢 ----------
(function legs() {
  for (const nm of ['thigh', 'shoulder']) {
    whiteBase(nm);
    sideShade(nm);
    const zf = nm === 'thigh' ? 4.75 : -7;
    const z0 = nm === 'thigh' ? 5.4 : -6.6, z1 = nm === 'thigh' ? 8.7 : -3.2;
    for (const dir of ['e', 'w']) {
      const rc = R[nm][dir];
      blob(rc, z0, z1, 7.4, 10.1, dir, zf, 6, 10.5, C.blush, C.blushEdge);
      grains(rc, 2);
    }
    fill(R[nm].d, C.down);
    grains(R[nm].n, 1); grains(R[nm].s, 1);
  }
  whiteBase('shin');
  sideShade('shin');
  grains(R.shin.e, 1); grains(R.shin.w, 1);
  fill(R.shin.d, C.down);

  for (const k of ['n', 'e', 'w', 's', 'u', 'd']) fill(R.hoof[k], C.hoof);
  for (const k of ['n', 'e', 'w', 's']) {
    const s = R.hoof[k];
    hline(s, 0, Math.ceil(s[2]) - 1, 0, C.hoofRim);
    hline(s, 0, Math.ceil(s[2]) - 1, Math.ceil(s[3]) - 1, C.hoofDark);
  }
  fill(R.hoof.d, C.hoofDark);
})();

// ---------- 卷尾：稻芽绿 ----------
(function tail() {
  whiteBase('tail_base');
  grains(R.tail_base.e, 1);
  grains(R.tail_base.w, 1);
  fill(R.tail_base.u, C.greenLight);
  for (const nm of ['tail_stem', 'curl_a', 'curl_b', 'curl_c', 'curl_d']) {
    for (const k of ['n', 'e', 'w', 's']) fill(R[nm][k], C.green);
    fill(R[nm].u, C.greenLight);
    fill(R[nm].d, C.greenDark);
  }
  for (const k of ['n', 'e', 'w', 's', 'u', 'd']) fill(R.curl_e[k], C.greenLight);
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
ihdr[8] = 8; ihdr[9] = 6;
const png = Buffer.concat([
  Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
  chunk('IHDR', ihdr),
  chunk('IDAT', zlib.deflateSync(raw, { level: 9 })),
  chunk('IEND', Buffer.alloc(0)),
]);
const outDir = __dirname;
fs.writeFileSync(path.join(outDir, 'mihengzhu.png'), png);

const plan = {};
for (const name of Object.keys(dims)) plan[name] = { off: offset[name], faces: R[name] };
fs.writeFileSync(path.join(outDir, 'pigplan.json'), JSON.stringify(plan));
console.log('OK packed height=' + totalH + ' elements=' + Object.keys(dims).length);
