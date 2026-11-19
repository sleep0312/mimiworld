# -*- coding: utf-8 -*-
# 米魂（rice_soul）音效合成——原为猴王设计的幽灵感一套，整体移交米魂使用
# 设计：长滑音+慢颤音+深混响的哀嚎/低吼质感，响度层级 ambient < death < hurt < roar
import numpy as np
import soundfile as sf
import os

SR = 44100
OUT = r"c:\mimiworld\src\main\resources\assets\mimiworld\sounds\mob\rice_soul"
rng = np.random.default_rng(20261113)


def f0_curve(pairs, n):
    """pairs: [(t_sec, hz), ...] -> 逐样本基频数组"""
    ts = np.array([p[0] for p in pairs])
    hs = np.array([p[1] for p in pairs])
    t = np.linspace(0, ts[-1], n)
    return np.interp(t, ts, hs)


def env_curve(pairs, n):
    """分段线性包络 + 5ms 平滑，消除爆音"""
    ts = np.array([p[0] for p in pairs])
    vs = np.array([p[1] for p in pairs])
    t = np.linspace(0, ts[-1], n)
    e = np.interp(t, ts, vs)
    w = int(0.005 * SR) | 1
    k = np.ones(w) / w
    return np.convolve(e, k, mode="same")


def glottal(f0, amp, rasp=0.0, growl=0.0, amf=30.0, harmonics=48):
    """带限喉源：谐波叠加 + 颤音由调用方在 f0 中体现；rasp 混入破音噪声；growl 加低频调幅"""
    n = len(f0)
    t = np.arange(n) / SR
    phase = 2 * np.pi * np.cumsum(f0) / SR
    x = np.zeros(n)
    for k in range(1, harmonics + 1):
        x += (1.0 / k ** 1.25) * np.sin(k * phase + 0.4 * np.sin(2 * np.pi * (0.6 * k) * t))
    x /= x.max() + 1e-9
    if growl > 0:
        x *= 1.0 + growl * np.sin(2 * np.pi * amf * t + 1.7 * np.sin(2 * np.pi * 0.9 * t))
    if rasp > 0:
        nz = rng.normal(0, 1, n)
        nz = np.convolve(nz, np.ones(4) / 4, mode="same")  # 轻度低通破音
        x = (1 - rasp) * x + rasp * 0.8 * nz * np.sign(x)
    return x * amp


def formants(x, fs):
    """频域共振峰：fs=[(fc,bw,gain),...]，洛伦兹乘积包络"""
    n = len(x)
    X = np.fft.rfft(x)
    f = np.fft.rfftfreq(n, 1 / SR)
    env = np.full(len(f), 1e-3)
    for fc, bw, g in fs:
        env = env * (g * bw ** 2 / ((f - fc) ** 2 + bw ** 2) + 1e-3)
    env /= env.max() + 1e-12
    return np.fft.irfft(X * env, n)


def reverb(x, wet=0.2, tail=1.2, tau=0.35):
    """Schroeder 风格混响：指数衰减噪声 IR + 稀疏梳状反射，FFT 卷积"""
    n_ir = int(tail * SR)
    tir = np.arange(n_ir) / SR
    ir = rng.normal(0, 1, n_ir) * np.exp(-tir / tau)
    for d, g in [(0.031, 0.5), (0.053, 0.35), (0.079, 0.25), (0.117, 0.18)]:
        i = int(d * SR)
        if i < n_ir:
            ir[i] += g
    ir /= np.sqrt(np.sum(ir ** 2)) + 1e-9
    y = np.fft.irfft(np.fft.rfft(x, n=len(x) + n_ir) * np.fft.rfft(ir, n=len(x) + n_ir))[: len(x) + n_ir]
    dry = x
    w = y[: len(x)]
    return (1 - wet) * dry + wet * w / (np.max(np.abs(w)) + 1e-9) * (np.max(np.abs(dry)) + 1e-9)


def normalize(x, rms_db):
    target = 10 ** (rms_db / 20)
    rms = np.sqrt(np.mean(x ** 2)) + 1e-12
    x = x * (target / rms)
    peak = np.max(np.abs(x))
    if peak > 0.95:
        x = 0.85 * np.tanh(x / 0.85)
    return x


def pad(x, total):
    n = len(x)
    if n >= total:
        return x[:total]
    return np.concatenate([x, np.zeros(total - n)])


def fade(x, ms=12):
    w = int(ms / 1000 * SR)
    x[:w] *= np.linspace(0, 1, w)
    x[-w:] *= np.linspace(1, 0, w)
    return x


def syllable(dur, f0_pairs, amp_pairs, fs, vib_hz, vib_depth, rasp=0.0, growl=0.0, amf=30.0):
    n = int(dur * SR)
    t = np.arange(n) / SR
    f0 = f0_curve(f0_pairs, n)
    f0 = f0 * (1 + vib_depth * np.sin(2 * np.pi * vib_hz * t + 0.7 * np.sin(2 * np.pi * vib_hz * 0.31 * t)))
    amp = env_curve(amp_pairs, n)
    return formants(glottal(f0, amp, rasp, growl, amf), fs)


def silence(dur):
    return np.zeros(int(dur * SR))


# 鼻音猴叫共振峰组（凶狠版：F1 偏低、F3 偏亮）
NASAL = [(640, 95, 1.0), (1210, 140, 0.55), (2560, 220, 0.30)]
BRIGHT = [(980, 120, 1.0), (1850, 180, 0.65), (3150, 260, 0.40)]
SCREECH = [(1180, 110, 1.0), (2350, 200, 0.70), (3550, 300, 0.45)]
DEEP = [(300, 90, 1.0), (760, 130, 0.55), (1480, 200, 0.28)]

# ---------- ambient：三段升调“吼—吼—吼” + 急促嘁嘁串叫 + 收尾短嘶 ----------
a1 = syllable(0.30, [(0, 320), (0.12, 385), (0.30, 340)], [(0, 0), (0.03, 1), (0.20, 0.8), (0.30, 0)], NASAL, 6.2, 0.030)
a2 = syllable(0.30, [(0, 400), (0.12, 470), (0.30, 415)], [(0, 0), (0.03, 1), (0.20, 0.8), (0.30, 0)], NASAL, 6.6, 0.032)
a3 = syllable(0.34, [(0, 500), (0.14, 590), (0.34, 505)], [(0, 0), (0.03, 1), (0.22, 0.8), (0.34, 0)], NASAL, 7.1, 0.035)
chats = []
for i in range(7):
    hi = 640 + (i % 3) * 130 + (60 if i > 3 else 0)
    chats.append(syllable(0.075, [(0, hi), (0.075, hi * 0.92)], [(0, 0), (0.008, 1), (0.05, 0.55), (0.075, 0)], BRIGHT, 9.0, 0.05, rasp=0.30))
chatter = np.concatenate([c if j == 0 else np.concatenate([silence(0.035), c]) for j, c in enumerate(chats)])
tail_scr = syllable(0.26, [(0, 940), (0.10, 1120), (0.26, 520)], [(0, 0), (0.02, 1), (0.16, 0.7), (0.26, 0)], SCREECH, 8.0, 0.06, rasp=0.20)
ambient = np.concatenate([a1, silence(0.09), a2, silence(0.09), a3, silence(0.20), chatter, silence(0.06), tail_scr])
ambient = reverb(normalize(fade(ambient), -20.0), wet=0.14, tail=0.8, tau=0.22)

# ---------- hurt：短促尖利嘶叫，重破音 ----------
h = syllable(0.42, [(0, 980), (0.06, 1130), (0.42, 400)], [(0, 0), (0.012, 1), (0.22, 0.75), (0.42, 0)], SCREECH, 11.0, 0.07, rasp=0.38)
hurt = reverb(normalize(fade(h), -13.0), wet=0.10, tail=0.5, tau=0.15)

# ---------- roar：低吼铺垫 + 半秒后拔起的长嘶吼，最大响度、最长混响 ----------
nr = int(3.0 * SR)
tr = np.arange(nr) / SR
g_f0 = f0_curve([(0, 118), (0.9, 158), (1.9, 142), (2.6, 104), (3.0, 92)], nr)
g_f0 *= 1 + 0.02 * np.sin(2 * np.pi * 4.5 * tr)
g_amp = env_curve([(0, 0), (0.35, 0.9), (1.8, 1.0), (2.5, 0.6), (3.0, 0)], nr)
growl_layer = formants(glottal(g_f0, g_amp, rasp=0.18, growl=0.45, amf=27.0), DEEP)

ns = int(2.1 * SR)
ts = np.arange(ns) / SR
s_f0 = f0_curve([(0, 470), (0.5, 760), (1.1, 1130), (1.7, 1050), (2.1, 640)], ns)
s_f0 *= 1 + (0.03 + 0.05 * np.clip(ts / 1.0, 0, 1)) * np.sin(2 * np.pi * 7.3 * ts + 1.1 * np.sin(2 * np.pi * 0.7 * ts))
s_amp = env_curve([(0, 0), (0.06, 0.55), (0.5, 1.0), (1.6, 0.9), (2.1, 0)], ns)
screech_layer = formants(glottal(s_f0, s_amp, rasp=0.22, growl=0.15, amf=41.0), BRIGHT)

roar = growl_layer
off = int(0.55 * SR)
roar[off:off + ns] += screech_layer
roar = reverb(normalize(fade(roar), -10.0), wet=0.30, tail=1.6, tau=0.5)

# ---------- death：悠长哀嚎滑落 + 低沉呻吟收尾 ----------
nd = int(3.3 * SR)
td = np.arange(nd) / SR
d_f0 = f0_curve([(0, 700), (0.5, 760), (1.9, 260), (2.2, 210), (3.3, 95)], nd)
d_f0 *= 1 + (0.025 + 0.05 * np.clip(td / 1.2, 0, 1)) * np.sin(2 * np.pi * 5.4 * td)
d_amp = env_curve([(0, 0), (0.08, 0.95), (1.0, 0.8), (2.2, 0.55), (3.0, 0.25), (3.3, 0)], nd)
death = formants(glottal(d_f0, d_amp, rasp=0.15, growl=0.25, amf=22.0), NASAL)
death = reverb(normalize(fade(death), -16.0), wet=0.24, tail=1.3, tau=0.42)

os.makedirs(OUT, exist_ok=True)
for name, data in [("ambient", ambient), ("hurt", hurt), ("roar", roar), ("death", death)]:
    data = np.asarray(data, dtype=np.float32)
    path = os.path.join(OUT, name + ".ogg")
    with sf.SoundFile(path, "w", samplerate=SR, channels=1, format="OGG", subtype="VORBIS") as f:
        f.write(data)
    rms = 20 * np.log10(np.sqrt(np.mean(data ** 2)) + 1e-12)
    print(f"{name:8s} dur={len(data)/SR:.2f}s rms={rms:.1f}dBFS peak={np.max(np.abs(data)):.2f} bytes={os.path.getsize(path)}")
