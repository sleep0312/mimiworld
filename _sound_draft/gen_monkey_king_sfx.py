# -*- coding: utf-8 -*-
# 米米猴王 Boss 音效合成 v2（干声猴叫版，替换幽灵感 v1，v1 已移交米魂）
# 去幽灵化要点：几乎无混响、短促音节、弹击口爆音起音、快速音高轮廓、颤音代滑音
# 结构参照真实猴叫：pant-hoot 呼吼串 + 急促嘁嘁连叫 + 嘶吼咯咯笑
import numpy as np
import soundfile as sf
import os

SR = 44100
OUT = r"c:\mimiworld\src\main\resources\assets\mimiworld\sounds\boss\mimi_monkey_king"
rng = np.random.default_rng(77001)


def f0_curve(pairs, n):
    ts = np.array([p[0] for p in pairs])
    hs = np.array([p[1] for p in pairs])
    t = np.linspace(0, ts[-1], n)
    return np.interp(t, ts, hs)


def env_curve(pairs, n):
    ts = np.array([p[0] for p in pairs])
    vs = np.array([p[1] for p in pairs])
    t = np.linspace(0, ts[-1], n)
    e = np.interp(t, ts, vs)
    w = int(0.003 * SR) | 1
    k = np.ones(w) / w
    return np.convolve(e, k, mode="same")


def glottal(f0, amp, rasp=0.0, growl=0.0, amf=30.0, trem=0.0, tremf=16.0, harmonics=40):
    n = len(f0)
    t = np.arange(n) / SR
    phase = 2 * np.pi * np.cumsum(f0) / SR
    x = np.zeros(n)
    for k in range(1, harmonics + 1):
        x += (1.0 / k ** 1.2) * np.sin(k * phase + 0.5 * np.sin(2 * np.pi * (0.7 * k) * t))
    x /= x.max() + 1e-9
    if trem > 0:  # 快颤（猴叫标志性 amplitude tremolo，区别于幽灵的慢滑音）
        x *= 1.0 + trem * np.sin(2 * np.pi * tremf * t + 0.9 * np.sin(2 * np.pi * 1.3 * t))
    if growl > 0:
        x *= 1.0 + growl * np.sin(2 * np.pi * amf * t + 1.7 * np.sin(2 * np.pi * 0.9 * t))
    if rasp > 0:
        nz = rng.normal(0, 1, n)
        nz = np.convolve(nz, np.ones(3) / 3, mode="same")  # 轻破音噪声
        x = (1 - rasp) * x + rasp * 0.85 * nz * np.sign(x)
    return x * amp


def formants(x, fs):
    n = len(x)
    X = np.fft.rfft(x)
    f = np.fft.rfftfreq(n, 1 / SR)
    env = np.full(len(f), 1e-3)
    for fc, bw, g in fs:
        env = env * (g * bw ** 2 / ((f - fc) ** 2 + bw ** 2) + 1e-3)
    env /= env.max() + 1e-12
    return np.fft.irfft(X * env, n)


def click(dur=0.035, fc=1750, level=1.0):
    """抿嘴弹击/辅音起音：短促带通噪声爆发，猴叫音节开头的“克”声"""
    n = int(dur * SR)
    t = np.arange(n) / SR
    x = rng.normal(0, 1, n) * np.exp(-t / 0.005)
    X = np.fft.rfft(x)
    f = np.fft.rfftfreq(n, 1 / SR)
    x = np.fft.irfft(X * np.exp(-((f - fc) / 950.0) ** 2), n)
    return x / (np.max(np.abs(x)) + 1e-9) * level


def breath(dur, level=0.3, fc=1400):
    """呼气噪声垫，替代混响提供空气感"""
    n = int(dur * SR)
    t = np.arange(n) / SR
    x = rng.normal(0, 1, n) * np.sin(np.pi * t / dur) ** 2
    X = np.fft.rfft(x)
    f = np.fft.rfftfreq(n, 1 / SR)
    x = np.fft.irfft(X * np.exp(-((f - fc) / 1300.0) ** 2), n)
    return x / (np.max(np.abs(x)) + 1e-9) * level


def dry(x, wet=0.06, tail=0.18):
    """极短房间感：小湿比例+短尾，避免幽灵式长混响"""
    n_ir = int(tail * SR)
    tir = np.arange(n_ir) / SR
    ir = rng.normal(0, 1, n_ir) * np.exp(-tir / (tail / 3.0))
    ir[0] += 1.0
    ir /= np.sqrt(np.sum(ir ** 2)) + 1e-9
    y = np.fft.irfft(np.fft.rfft(x, n=len(x) + n_ir) * np.fft.rfft(ir, n=len(x) + n_ir))[: len(x)]
    return (1 - wet) * x + wet * y


def normalize(x, rms_db):
    target = 10 ** (rms_db / 20)
    x = x * (target / (np.sqrt(np.mean(x ** 2)) + 1e-12))
    if np.max(np.abs(x)) > 0.95:
        x = 0.85 * np.tanh(x / 0.85)
    return x


def fade(x, ms=8):
    w = int(ms / 1000 * SR)
    x[:w] *= np.linspace(0, 1, w)
    x[-w:] *= np.linspace(1, 0, w)
    return x


NASAL = [(620, 80, 1.0), (1180, 110, 0.60), (2450, 170, 0.32)]
BRIGHT = [(900, 100, 1.0), (1650, 150, 0.70), (2800, 220, 0.42)]
SCREECH = [(1050, 90, 1.0), (2100, 160, 0.75), (3300, 260, 0.48)]
MID = [(800, 110, 1.0), (1500, 160, 0.60), (2600, 240, 0.35)]
DEEP = [(300, 80, 1.0), (740, 120, 0.50), (1420, 180, 0.26)]


def whoop(dur, f0_pairs, fs, vib_hz=8.5, vib_depth=0.022, click_fc=1750, rasp=0.12, trem=0.18):
    """单击起音 + 快升快降的呼吼音节"""
    n = int(dur * SR)
    t = np.arange(n) / SR
    f0 = f0_curve(f0_pairs, n)
    f0 = f0 * (1 + vib_depth * np.sin(2 * np.pi * vib_hz * t))
    amp = env_curve([(0, 0), (0.015, 1), (dur * 0.55, 0.85), (dur, 0)], n)
    x = formants(glottal(f0, amp, rasp=rasp, trem=trem), fs)
    c = click(0.035, click_fc, 0.5)
    x[: len(c)] += c * amp[: len(c)] * 1.6
    return x


def kek(dur, f0v, fs, rasp=0.4, click_fc=2100):
    """急促嘁嘁单音：爆音开头 + 极短嘶鸣"""
    n = int(dur * SR)
    t = np.arange(n) / SR
    f0 = f0_curve([(0, f0v), (dur, f0v * 0.9)], n) * (1 + 0.04 * np.sin(2 * np.pi * 12 * t))
    amp = env_curve([(0, 0), (0.006, 1), (dur * 0.6, 0.6), (dur, 0)], n)
    x = formants(glottal(f0, amp, rasp=rasp, harmonics=28), fs)
    c = click(0.025, click_fc, 0.8)
    x[: len(c)] += c
    return x


def seq(items, gap=0.0, gap_sound=None):
    parts = []
    for i, x in enumerate(items):
        parts.append(x)
        if i < len(items) - 1 and gap > 0:
            parts.append(gap_sound(gap) if gap_sound else np.zeros(int(gap * SR)))
    return np.concatenate(parts)


# ---------- ambient：pant-hoot 呼吼四连（音高递增、间隔收紧）→ 嘁嘁串 → 呼气收尾 ----------
w1 = whoop(0.24, [(0, 360), (0.10, 470), (0.24, 380)], NASAL)
w2 = whoop(0.22, [(0, 430), (0.09, 560), (0.22, 450)], NASAL)
w3 = whoop(0.20, [(0, 520), (0.08, 680), (0.20, 540)], BRIGHT)
w4 = whoop(0.22, [(0, 600), (0.09, 810), (0.22, 620)], BRIGHT, rasp=0.2)
hoots = seq([w1, w2, w3, w4], gap=0.06, gap_sound=lambda d: breath(d, 0.18))
keks = seq([kek(0.06, 980 + (i % 2) * 260, BRIGHT) for i in range(9)], gap=0.035)
ambient = np.concatenate([hoots, breath(0.08, 0.25), keks, breath(0.22, 0.22, 1100)])
ambient = fade(dry(normalize(ambient, -20.0), wet=0.05, tail=0.15))

# ---------- hurt：单击硬“克！”短嘶，中频、快衰减 ----------
h = whoop(0.30, [(0, 800), (0.05, 940), (0.30, 520)], MID, vib_hz=11, vib_depth=0.04, click_fc=1500, rasp=0.35, trem=0.25)
hurt = fade(dry(normalize(h, -13.0), wet=0.04, tail=0.12))

# ---------- roar：加速咯咯嘶笑 12 连 + 长尖啸收尾（低吼垫底） ----------
n_syl = 12
cackle = []
gap = 0.24
for i in range(n_syl):
    base = 680 + i * 85
    syl = whoop(0.10 + 0.02 * (i % 2), [(0, base), (0.05, base * 1.35), (0.12, base * 0.95)],
                SCREECH, vib_hz=13, vib_depth=0.05, click_fc=2300, rasp=0.3, trem=0.3)
    cackle.append(syl)
    gap = max(0.09, gap - 0.014)
    cackle.append(np.zeros(int(gap * SR)))
n_scr = int(0.75 * SR)
ts = np.arange(n_scr) / SR
s_f0 = f0_curve([(0, 1500), (0.3, 1320), (0.75, 920)], n_scr)
s_f0 = s_f0 * (1 + 0.08 * np.sin(2 * np.pi * 11 * ts))
s_amp = env_curve([(0, 0), (0.03, 1), (0.5, 0.9), (0.75, 0)], n_scr)
scream = formants(glottal(s_f0, s_amp, rasp=0.45, trem=0.2, tremf=18), SCREECH)
n_g = int(1.1 * SR)
tg = np.arange(n_g) / SR
g_f0 = f0_curve([(0, 170), (0.5, 145), (1.1, 115)], n_g)
g_amp = env_curve([(0, 0), (0.15, 0.4), (0.8, 0.3), (1.1, 0)], n_g)
growl = formants(glottal(g_f0, g_amp, rasp=0.2, growl=0.5, amf=34), DEEP)
# 组合：咯咯嘶笑串 + 长尖啸，尖啸段叠低吼垫
body = np.concatenate(cackle)
mix = np.zeros(len(body) + n_scr)
mix[: len(body)] += body
mix[len(body): len(body) + n_scr] += scream
mix[len(body): len(body) + min(n_g, n_scr)] += growl[: min(n_g, n_scr)]
roar = fade(dry(normalize(mix, -10.0), wet=0.10, tail=0.30))

# ---------- death：两声下滑痛呼 + 气声咯咯消散（干声） ----------
d1 = whoop(0.55, [(0, 660), (0.12, 740), (0.55, 330)], NASAL, vib_hz=9, vib_depth=0.05, rasp=0.25, trem=0.2)
d2 = whoop(0.65, [(0, 540), (0.12, 600), (0.65, 230)], NASAL, vib_hz=8, vib_depth=0.06, rasp=0.3, trem=0.22)
n_tail = int(0.85 * SR)
tt = np.arange(n_tail) / SR
t_f0 = f0_curve([(0, 210), (0.85, 120)], n_tail) * (1 + 0.03 * np.sin(2 * np.pi * 6 * tt))
t_amp = env_curve([(0, 0.5), (0.2, 0.35), (0.85, 0)], n_tail)
gasp = formants(glottal(t_f0, t_amp, rasp=0.6, growl=0.3, amf=24, harmonics=18), DEEP)
death = np.concatenate([d1, breath(0.14, 0.2), d2, breath(0.10, 0.18), gasp])
death = fade(dry(normalize(death, -16.0), wet=0.07, tail=0.2))

os.makedirs(OUT, exist_ok=True)
for name, data in [("ambient", ambient), ("hurt", hurt), ("roar", roar), ("death", death)]:
    data = np.asarray(data, dtype=np.float32)
    path = os.path.join(OUT, name + ".ogg")
    with sf.SoundFile(path, "w", samplerate=SR, channels=1, format="OGG", subtype="VORBIS") as f:
        f.write(data)
    rms = 20 * np.log10(np.sqrt(np.mean(data ** 2)) + 1e-12)
    print(f"{name:8s} dur={len(data)/SR:.2f}s rms={rms:.1f}dBFS peak={np.max(np.abs(data)):.2f} bytes={os.path.getsize(path)}")
