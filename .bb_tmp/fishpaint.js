// 贪米鱼纹理生成器：box UV 打包 + 大米主题（饭粒白/稻黄金/稻芽绿/贪吃腮红）
const fs = require('fs');
const zlib = require('zlib');
const path = require('path');

const W = 64, H = 32;

// name: [x, y, z, xfrom, yfrom, zfrom]（对称件左右共用同一 UV 槽）
const dims = {
  body:               [4, 5, 6, -2, 0, -3],
  head:               [4, 4, 4, -2, 1, -7],
  caudal_fin:         [0, 3, 3, 0, 0, 8],
  dorsal_fin_front:   [0, 2, 4, 0, 5, -2],
  caudal_peduncle:    [2, 3, 3, -1, 1, 5],
  pectoral_fin:       [4, 0, 4, 2, 2, -2],
  body_taper:         [4, 3, 2, -2, 1, 3],
  snout:              [2, 2, 2, -1, 2, -9],
  anal_fin:           [0, 1, 3, 0, -1, 1],
  pelvic_fin:         [1, 2, 2, 0, -2, -3],
  dorsal_fin_mid:     [0, 1, 2, 0, 4, 3],
  dorsal_fin_rear:    [0, 1, 1, 0, 4, 6],
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
function idx(px, py) { return (py * W + px) * 4; }
function setPx(px, py, c, a = 255) {
  px = px | 0; py = py | 0;
  if (px < 0 || py < 0 || px >= W || py >= H) return;
  const i = idx(px, py);
  buf[i] = c[0]; buf[i + 1] = c[1]; buf[i + 2] = c[2]; buf[i + 3] = a;
}
function fill(rc, c) {
  const [x, y, w, h] = rc;
  for (let py = y | 0; py < Math.ceil(y + h); py++)
    for (let px = x | 0; px < Math.ceil(x + w); px++) setPx(px, py, c);
}
let _seed = 424242;
function rnd() { _seed = (_seed * 1664525 + 1013904223) >>> 0; return _seed / 4294967296; }

// 世界坐标 -> 侧面(e/w)像素
function sideCol(rc, dir, wz, zfrom) {
  return dir === 'w' ? rc[0] + (wz - zfrom) : rc[0] + (rc[2] - (wz - zfrom));
}
function sidePx(rc, dir, wz, wy, name) {
  const d = dims[name];
  const cx = sideCol(rc, dir, wz, d[5]);
  const cy = rc[1] + (d[1] + d[4] - wy);
  return [cx | 0, cy | 0];
}
// 在侧面世界单元(mz 单元中心 z, my 单元中心 y)打点
function sideDot(name, dir, mz, my, c) {
  const rc = R[name][dir];
  const [px, py] = sidePx(rc, dir, mz, my, name);
  setPx(px, py, c);
}
// n/s 面世界 (x,y)
function faceXYPx(name, face, wx, wy) {
  const rc = R[name][face];
  const d = dims[name];
  let px;
  if (face === 'n') px = rc[0] + (wx - d[3]);
  else px = rc[0] + rc[2] - (wx - d[3]) - 1;
  const py = rc[1] + (d[4] + d[1] - wy);
  return [px | 0, py | 0];
}
function faceXYDot(name, face, mx, my, c) {
  const [px, py] = faceXYPx(name, face, mx, my);
  setPx(px, py, c);
}
// u/d 面世界 (x,z)：py 顶行 = zfrom
function faceXZPx(name, face, wx, wz) {
  const rc = R[name][face];
  const d = dims[name];
  let px;
  if (face === 'u') px = rc[0] + (wx - d[3]);
  else px = rc[0] + rc[2] - (wx - d[3]) - 1;
  const py = rc[1] + (wz - d[5]);
  return [px | 0, py | 0];
}
function faceXZDot(name, face, mx, mz, c) {
  const [px, py] = faceXZPx(name, face, mx, mz);
  setPx(px, py, c);
}

// ---------- 大米主题调色板 ----------
const C = {
  backDark:  [193, 164, 92],
  back:      [221, 201, 134],
  backLight: [240, 228, 180],
  body:      [244, 232, 200],
  bodyShade: [228, 214, 182],
  belly:     [253, 250, 242],
  bellyShade:[224, 210, 178],
  scaleGold: [236, 222, 168],
  lineGold:  [206, 175, 96],
  lineGoldL: [228, 204, 130],
  grainHi:   [255, 253, 246],
  grainEdge: [226, 212, 168],
  finGreen:  [162, 186, 108],
  finDark:   [122, 148, 78],
  finLight:  [200, 218, 152],
  finBase:   [226, 212, 158],
  eyeBlack:  [38, 32, 28],
  eyeWhite:  [255, 255, 255],
  blush:     [242, 184, 160],
  gill:      [222, 200, 132],
  lip:       [204, 156, 118],
  lipDark:   [176, 128, 94],
};

const R = {};
for (const name of Object.keys(dims)) R[name] = rects(name);

function baseAll(name, c) {
  for (const k of ['n', 'e', 'w', 's', 'u', 'd']) fill(R[name][k], c);
}

// 侧面鱼身渐变：按世界 y 行
function sideBody(name, dir, rows) {
  // rows: [{y0,y1,c}] 世界 y 区间上色
  const d = dims[name];
  const rc = R[name][dir];
  for (let mz = d[5]; mz < d[5] + d[2]; mz++) {
    for (let my = d[4]; my < d[4] + d[1]; my++) {
      const cy = my + 0.5;
      let col = null;
      for (const r of rows) if (cy > r.y0 && cy <= r.y1) { col = r.c; break; }
      if (col) sideDot(name, dir, mz + 0.5, cy, col);
    }
  }
}

const BODY_ROWS = [
  { y0: 4, y1: 5, c: C.backDark },
  { y0: 3, y1: 4, c: C.back },
  { y0: 2, y1: 3, c: C.body },
  { y0: 1, y1: 2, c: C.bodyShade },
  { y0: 0, y1: 1, c: C.bellyShade },
];
const BODY_ROWS_T = [
  { y0: 3, y1: 4, c: C.backDark },
  { y0: 2, y1: 3, c: C.back },
  { y0: 1, y1: 2, c: C.bodyShade },
];
const HEAD_ROWS = [
  { y0: 4, y1: 5, c: C.backDark },
  { y0: 3, y1: 4, c: C.back },
  { y0: 2, y1: 3, c: C.body },
  { y0: 1, y1: 2, c: C.bodyShade },
];

// 米粒：侧面（横向 2px 白粒 + 下沿金边）
function grainSide(name, dir, mz, my) {
  sideDot(name, dir, mz, my, C.grainHi);
  sideDot(name, dir, mz + 1, my, C.grainHi);
  sideDot(name, dir, mz, my - 1, C.grainEdge);
}

// ================= 躯干 body =================
(function () {
  baseAll('body', C.body);
  for (const dir of ['e', 'w']) {
    sideBody('body', dir, BODY_ROWS);
    // 金侧线（体侧中线，虚线点）
    const seq = [-2.5, -1.5, -0.5, 0.5, 1.5, 2.5];
    seq.forEach((z, i) => sideDot('body', dir, z, 1.5, i % 2 ? C.lineGoldL : C.lineGold));
    // 米粒鳞片（交错三粒）
    grainSide('body', dir, -2.2 + 0.5, 3.5);
    grainSide('body', dir, 0.8 + 0.5, 3.3);
    sideDot('body', dir, -0.5, 2.5, C.grainHi);
    sideDot('body', dir, -1.5, 2.5, C.grainEdge);
    sideDot('body', dir, 2.2 + 0.5 - 1, 2.5, C.grainEdge);
    sideDot('body', dir, 0.5, 3.5, C.scaleGold);
    sideDot('body', dir, 0.5, 2.5, C.scaleGold);
    sideDot('body', dir, 2.5, 2.5, C.scaleGold);
  }
  // 背 u：稻金甲 + 中轴脊线 + 米粒
  fill(R.body.u, C.back);
  for (const mz of [-2.5, -0.5, 1.5]) {
    faceXZDot('body', 'u', -0.5, mz, C.lineGold);
    faceXZDot('body', 'u', 0.5, mz, C.lineGold);
  }
  faceXZDot('body', 'u', -1.5, -1.5, C.grainHi);
  faceXZDot('body', 'u', 1.5, 0.8 + 0.5, C.grainHi);
  faceXZDot('body', 'u', -0.5, 2.2 + 0.5, C.grainHi);
  // 腹 d
  fill(R.body.d, C.belly);
  // n/s 衔接面（被相邻块遮挡，给基础渐变）
  fill(R.body.n, C.body); fill(R.body.s, C.body);
})();

// ================= 收分节 body_taper =================
(function () {
  baseAll('body_taper', C.body);
  for (const dir of ['e', 'w']) {
    sideBody('body_taper', dir, BODY_ROWS_T);
    sideDot('body_taper', dir, 3.5, 1.5, C.lineGold);
    sideDot('body_taper', dir, 4.5, 1.5, C.lineGoldL);
    sideDot('body_taper', dir, 4.5, 2.5, C.grainHi);
    sideDot('body_taper', dir, 3.5, 2.5, C.scaleGold);
  }
  fill(R.body_taper.u, C.back);
  faceXZDot('body_taper', 'u', -0.5, 3.5, C.lineGold);
  faceXZDot('body_taper', 'u', 0.5, 4.5, C.lineGold);
  fill(R.body_taper.d, C.belly);
})();

// ================= 尾柄 caudal_peduncle =================
(function () {
  baseAll('caudal_peduncle', C.body);
  for (const dir of ['e', 'w']) {
    sideBody('caudal_peduncle', dir, BODY_ROWS_T);
    sideDot('caudal_peduncle', dir, 5.5, 1.5, C.lineGold);
    sideDot('caudal_peduncle', dir, 6.5, 1.5, C.lineGoldL);
    sideDot('caudal_peduncle', dir, 7.5, 1.5, C.lineGold);
    sideDot('caudal_peduncle', dir, 5.5, 2.5, C.scaleGold);
    sideDot('caudal_peduncle', dir, 7.5, 2.5, C.scaleGold);
  }
  fill(R.caudal_peduncle.u, C.back);
  faceXZDot('caudal_peduncle', 'u', -0.5, 6.5, C.lineGold);
  fill(R.caudal_peduncle.d, C.belly);
  // 尾端 s 面：鳍基米黄，接尾鳍
  fill(R.caudal_peduncle.s, C.finBase);
})();

// ================= 头部 head =================
(function () {
  baseAll('head', C.body);
  for (const dir of ['e', 'w']) {
    sideBody('head', dir, HEAD_ROWS);
    // 大眼睛 2x2（前上方），前上格白点高光
    sideDot('head', dir, -6.5, 3.5, C.eyeWhite);
    sideDot('head', dir, -5.5, 3.5, C.eyeBlack);
    sideDot('head', dir, -6.5, 2.5, C.eyeBlack);
    sideDot('head', dir, -5.5, 2.5, C.eyeBlack);
    // 鳃盖金线（眼后弧形两点）
    sideDot('head', dir, -4.5, 2.5, C.gill);
    sideDot('head', dir, -3.5, 2.0 + 0.5, C.grainEdge);
    // 贪吃腮红
    sideDot('head', dir, -4.5, 1.5, C.blush);
    sideDot('head', dir, -3.5, 3.5, C.scaleGold);
  }
  // 脸 n 面（中部被 snout 遮挡）：头顶金甲、两侧眼内缘+高光、下巴米白
  fill(R.head.n, C.body);
  for (let mx = -1.5; mx <= 1.5; mx += 1) {
    faceXYDot('head', 'n', mx, 4.5, C.backDark);
    faceXYDot('head', 'n', -mx, 4.5, C.backDark);
  }
  faceXYDot('head', 'n', -1.5, 3.5, C.eyeBlack);
  faceXYDot('head', 'n', 1.5, 3.5, C.eyeBlack);
  faceXYDot('head', 'n', -1.5, 1.5, C.belly);
  faceXYDot('head', 'n', 1.5, 1.5, C.belly);
  // 头顶 u：金甲 + 脊点
  fill(R.head.u, C.back);
  faceXZDot('head', 'u', -0.5, -4.5, C.lineGold);
  faceXZDot('head', 'u', 0.5, -5.5, C.lineGold);
  faceXZDot('head', 'u', -1.5, -6.5, C.backLight);
  fill(R.head.d, C.belly);
})();

// ================= 吻部 snout =================
(function () {
  baseAll('snout', C.body);
  for (const dir of ['e', 'w']) {
    sideDot('snout', dir, -8.5, 3.5, C.body);
    sideDot('snout', dir, -7.5, 3.5, C.body);
    // 上扬嘴角：前下 lip，后下浅金
    sideDot('snout', dir, -8.5, 2.5, C.lip);
    sideDot('snout', dir, -7.5, 2.5, C.lipDark);
  }
  // 正面嘴：上吻白 + 下唇缝
  faceXYDot('snout', 'n', -0.5, 3.5, C.body);
  faceXYDot('snout', 'n', 0.5, 3.5, C.body);
  faceXYDot('snout', 'n', -0.5, 2.5, C.lipDark);
  faceXYDot('snout', 'n', 0.5, 2.5, C.lipDark);
  // 鼻尖亮
  fill(R.snout.u, C.body);
  faceXZDot('snout', 'u', -0.5, -8.5, C.grainHi);
  faceXZDot('snout', 'u', 0.5, -7.8 + 0.3, C.grainEdge);
  fill(R.snout.d, C.belly);
})();

// ================= 背鳍/臀鳍（薄板，画 e/w） =================
(function () {
  // 第一背鳍 4x2：底行鳍基米黄，顶行浅绿+两根深鳍棘
  for (const dir of ['e', 'w']) {
    const rc = R.dorsal_fin_front[dir];
    fill(rc, C.finBase);
    const cells = [C.finLight, C.finDark, C.finGreen, C.finDark];
    cells.forEach((c, i) => {
      const wz = dims.dorsal_fin_front[5] + i + 0.5;
      sideDot('dorsal_fin_front', dir, wz, 6.5, c);
    });
  }
  // 第二背鳍 2x1
  for (const dir of ['e', 'w']) {
    sideDot('dorsal_fin_mid', dir, 3.5, 4.5, C.finGreen);
    sideDot('dorsal_fin_mid', dir, 4.5, 4.5, C.finDark);
  }
  // 第三小背鳍 1x1
  sideDot('dorsal_fin_rear', 'e', 6.5, 4.5, C.finDark);
  sideDot('dorsal_fin_rear', 'w', 6.5, 4.5, C.finDark);
  // 臀鳍 3x1：前棘深
  for (const dir of ['e', 'w']) {
    sideDot('anal_fin', dir, 1.5, -0.5, C.finDark);
    sideDot('anal_fin', dir, 2.5, -0.5, C.finGreen);
    sideDot('anal_fin', dir, 3.5, -0.5, C.finGreen);
  }
})();

// ================= 腹鳍 pelvic（左右共用） =================
(function () {
  baseAll('pelvic_fin', C.finGreen);
  for (const dir of ['e', 'w']) {
    sideDot('pelvic_fin', dir, -2.5, -0.5, C.finBase);
    sideDot('pelvic_fin', dir, -1.5, -0.5, C.finBase);
    sideDot('pelvic_fin', dir, -2.5, -1.5, C.finGreen);
    sideDot('pelvic_fin', dir, -1.5, -1.5, C.finDark);
  }
})();

// ================= 胸鳍 pectoral（水平扇，画 u/d） =================
(function () {
  fill(R.pectoral_fin.u, C.finGreen);
  fill(R.pectoral_fin.d, C.finGreen);
  // u：4 列（基部->外缘）x 4 行（z），基部米黄、外缘浅绿、鳍条深绿
  const rows = [
    [C.finBase, C.finDark, C.finGreen, C.finLight],
    [C.finBase, C.finGreen, C.finDark, C.finLight],
    [C.finBase, C.finGreen, C.finDark, C.finLight],
    [C.finBase, C.finDark, C.finGreen, C.finLight],
  ];
  rows.forEach((cells, zi) => {
    const wz = -2 + zi + 0.5;
    cells.forEach((c, xi) => {
      const wx = 2 + xi + 0.5;
      faceXZDot('pectoral_fin', 'u', wx, wz, c);
    });
  });
  // d：整体深一阶，外缘最深
  for (let zi = 0; zi < 4; zi++) {
    const wz = -2 + zi + 0.5;
    for (let xi = 0; xi < 4; xi++) {
      const wx = 2 + xi + 0.5;
      faceXZDot('pectoral_fin', 'd', wx, wz, xi === 3 ? C.finDark : C.finGreen);
    }
  }
})();

// ================= 尾鳍 caudal_fin（上下片共用，画 e/w） =================
(function () {
  const grid = [
    [C.finDark, C.finGreen, C.finDark],
    [C.finDark, C.finLight, C.finDark],
    [C.finDark, C.finGreen, C.finDark],
  ];
  for (const dir of ['e', 'w']) {
    grid.forEach((cells, li) => {
      const ly = li + 0.5;
      cells.forEach((c, zi) => {
        const wz = 8 + zi + 0.5;
        const d = dims.caudal_fin;
        const rc = R.caudal_fin[dir];
        const cx = sideCol(rc, dir, wz, d[5]) | 0;
        const cy = rc[1] + (d[1] - ly) | 0;
        setPx(cx, cy, c);
      });
    });
  }
})();

// ---------- PNG 编码 ----------
function crc32(b) {
  let c, crc = 0xffffffff;
  for (let i = 0; i < b.length; i++) {
    c = (crc ^ b[i]) & 0xff;
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
fs.writeFileSync(path.join(outDir, 'tanmiyu.png'), png);

// cube 名 -> UV 槽（对称件共用）
const cubeMap = {
  head: 'head', snout: 'snout', body: 'body', body_taper: 'body_taper',
  caudal_peduncle: 'caudal_peduncle', dorsal_fin_front: 'dorsal_fin_front',
  dorsal_fin_mid: 'dorsal_fin_mid', dorsal_fin_rear: 'dorsal_fin_rear',
  anal_fin: 'anal_fin', pelvic_fin_left: 'pelvic_fin', pelvic_fin_right: 'pelvic_fin',
  pectoral_fin_left: 'pectoral_fin', pectoral_fin_right: 'pectoral_fin',
  caudal_fin_upper: 'caudal_fin', caudal_fin_lower: 'caudal_fin',
};
const plan = { W, H, cubeMap, offset, faces: {} };
for (const name of Object.keys(dims)) plan.faces[name] = R[name];
fs.writeFileSync(path.join(outDir, 'fishplan.json'), JSON.stringify(plan));
console.log('OK packed height=' + totalH + ' slots=' + Object.keys(dims).length);
