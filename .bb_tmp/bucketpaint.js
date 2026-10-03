// 贪米鱼桶 16x16 物品贴图：铁桶 + 水 + 金白小米鱼（绿尾），侧视图
const fs = require('fs');
const zlib = require('zlib');
const path = require('path');

const W = 16, H = 16;
const buf = Buffer.alloc(W * H * 4, 0); // 全透明
function setPx(px, py, c, a = 255) {
  if (px < 0 || py < 0 || px >= W || py >= H) return;
  const i = (py * W + px) * 4;
  buf[i] = c[0]; buf[i + 1] = c[1]; buf[i + 2] = c[2]; buf[i + 3] = a;
}
// 字符调色板
const C = {
  D:  [84, 84, 88],    // 桶沿深铁
  L:  [190, 190, 194],  // 桶沿亮铁
  H:  [222, 222, 226],  // 高光
  W:  [54, 96, 168],    // 水（深）
  w:  [92, 138, 206],   // 水（亮面）
  B:  [142, 142, 148],  // 桶身
  b:  [162, 162, 168],  // 桶身高光
  S:  [104, 104, 110],  // 桶身阴影
  K:  [60, 60, 64],     // 桶底最暗
  G:  [224, 184, 86],   // 鱼金背
  g:  [193, 150, 60],   // 鱼暗金（背鳍）
  F:  [247, 241, 227],  // 鱼米白腹
  T:  [107, 133, 72],   // 鱼绿尾/鳍
  E:  [34, 30, 26],     // 鱼眼
};
const rows = [
  '................', // y0
  '................', // y1
  '................', // y2
  '................', // y3
  '.....DDDDDD.....', // y4
  '....DLHHLLLD....', // y5
  '....DwGGggWD....', // y6 水面 + 背鳍
  '....DTGGGGED....', // y7 绿尾-金身-头眼
  '....DTFTGGGD....', // y8 绿尾/白腹/绿胸鳍
  '....DBbbbbBD....', // y9 桶身（高光）
  '.....DBBBBD.....', // y10
  '.....DSSSSD.....', // y11
  '......DSSD......', // y12
  '.......KK.......', // y13
  '................', // y14
  '................', // y15
];
// y7 微调：x5 尾、x6-8 身、x9 眼；y8：x5 尾、x6 白、x7 胸鳍、x8-9 金
for (let y = 0; y < H; y++) {
  const r = rows[y];
  for (let x = 0; x < W; x++) {
    const ch = r[x];
    if (ch !== '.') setPx(x, y, C[ch]);
  }
}
setPx(5, 6, C.W);   // 水面两端深色
setPx(10, 6, C.W);
setPx(5, 7, C.T);
setPx(6, 7, C.G);
setPx(7, 7, C.G);
setPx(8, 7, C.G);
setPx(9, 7, C.E);
setPx(5, 8, C.T);
setPx(6, 8, C.F);
setPx(7, 8, C.T);
setPx(8, 8, C.G);
setPx(9, 8, C.G);

// ---------- PNG 编码 ----------
function crc32(b) {
  let c, crc = 0xffffffff;
  for (let n = 0; n < b.length; n++) {
    c = (crc ^ b[n]) & 0xff;
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
const ihdr = Buffer.alloc(13);
ihdr.writeUInt32BE(W, 0); ihdr.writeUInt32BE(H, 4);
ihdr[8] = 8; ihdr[9] = 6; ihdr[10] = 0; ihdr[11] = 0; ihdr[12] = 0;
const raw = Buffer.alloc((W * 4 + 1) * H);
for (let y = 0; y < H; y++) {
  raw[y * (W * 4 + 1)] = 0;
  buf.copy(raw, y * (W * 4 + 1) + 1, y * W * 4, (y + 1) * W * 4);
}
const png = Buffer.concat([
  Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
  chunk('IHDR', ihdr),
  chunk('IDAT', zlib.deflateSync(raw, { level: 9 })),
  chunk('IEND', Buffer.alloc(0)),
]);
const out1 = path.join(__dirname, 'tanmi_fish_bucket.png');
fs.writeFileSync(out1, png);
const out2 = path.join(__dirname, '..', 'src', 'main', 'resources', 'assets', 'mimiworld', 'textures', 'item', 'tanmi_fish_bucket.png');
fs.writeFileSync(out2, png);
console.log('written', out1, out2, png.length, 'bytes');
