const fs = require('fs');
const old = JSON.parse(fs.readFileSync('c:/mimiworld/_tmp_hualingmi/atlas.json', 'utf8'));
const BIN = 128, PAD = 1;
const FK = ['north', 'south', 'east', 'west', 'up', 'down'];
const rects = [];
for (const c of old.cubes) {
  for (const fk of FK) {
    const w = (fk === 'north' || fk === 'south' || fk === 'up' || fk === 'down') ? c.x : c.z;
    const h = (fk === 'up' || fk === 'down') ? c.z : c.y;
    rects.push({ name: c.name, group: c.group, role: c.role, key: fk, w, h });
  }
}
rects.sort((a, b) => (Math.max(b.w, b.h) - Math.max(a.w, a.h)) || (b.w * b.h - a.w * a.h));
let sky = [{ x: 0, y: 0, w: BIN }];
let fails = 0, maxY = 0;
function place(r) {
  const pw = r.w + PAD, ph = r.h + PAD;
  let best = null;
  for (let i = 0; i < sky.length; i++) {
    const x = sky[i].x;
    if (x + pw > BIN + 1e-6) continue;
    let yTop = 0, covered = 0, ok = false;
    for (let j = i; j < sky.length; j++) {
      yTop = Math.max(yTop, sky[j].y);
      covered = sky[j].x + sky[j].w - x;
      if (covered >= pw - 1e-6) { ok = true; break; }
    }
    if (ok && yTop + ph <= BIN + 1e-6) {
      if (!best || yTop < best.y || (Math.abs(yTop - best.y) < 1e-9 && x < best.x)) best = { x, y: yTop };
    }
  }
  if (!best) { fails++; r.px = -1; return; }
  r.px = best.x; r.py = best.y;
  maxY = Math.max(maxY, best.y + ph);
  const nx = best.x, nw = pw;
  let out = [];
  for (const s of sky) {
    if (nx >= s.x + s.w || nx + nw <= s.x) { out.push(s); continue; }
    if (nx > s.x) out.push({ x: s.x, y: s.y, w: nx - s.x });
    if (nx + nw < s.x + s.w) out.push({ x: nx + nw, y: s.y, w: s.x + s.w - (nx + nw) });
    out.push({ x: Math.max(nx, s.x), y: Math.max(s.y, best.y + ph), w: Math.min(nx + nw, s.x + s.w) - Math.max(nx, s.x) });
  }
  out = out.filter(s => s.w > 0.01);
  out.sort((a, b) => a.x - b.x);
  const merged = [];
  for (const s of out) {
    const m = merged[merged.length - 1];
    if (m && Math.abs(m.x + m.w - s.x) < 1e-6 && Math.abs(m.y - s.y) < 1e-6) m.w += s.w;
    else merged.push({ ...s });
  }
  sky = merged;
}
for (const r of rects) place(r);
const byCube = new Map();
let mn = 1e9, mx = -1e9;
for (const r of rects) {
  const k = r.name + '|' + r.group;
  if (!byCube.has(k)) byCube.set(k, { name: r.name, group: r.group, role: r.role, faces: {} });
  byCube.get(k).faces[r.key] = { x: r.px, y: r.py, w: r.w, h: r.h };
  if (r.px >= 0) { mn = Math.min(mn, r.px, r.py); mx = Math.max(mx, r.px + r.w, r.py + r.h); }
}
fs.writeFileSync('c:/mimiworld/_tmp_hualingmi/atlas2.json', JSON.stringify({ BIN, cubes: [...byCube.values()] }));
console.log(JSON.stringify({ rects: rects.length, fails, maxY: Math.round(maxY * 100) / 100, min: mn, max: Math.round(mx * 100) / 100, skySegs: sky.length }));
