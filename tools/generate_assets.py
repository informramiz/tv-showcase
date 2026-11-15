#!/usr/bin/env python3
"""Generates the bundled slideshow scenes and the ambient loop. Own-generated, no third-party media.

Usage: python3 tools/generate_assets.py   (needs Pillow, numpy, ffmpeg)
"""
import math
import pathlib
import subprocess

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

ROOT = pathlib.Path(__file__).resolve().parent.parent
RES = ROOT / "app/src/main/res"
W, H = 1920, 1080

# name: (sky top, sky bottom, sun colour, [ridge colours far -> near], stars, sun x)
SCENES = {
    "scene_dawn": ((38, 40, 92), (255, 160, 122), (255, 226, 170), [(120, 96, 140), (86, 70, 112), (52, 44, 82), (28, 26, 54)], False, 0.62),
    "scene_dusk": ((24, 20, 64), (232, 98, 96), (255, 190, 120), [(110, 54, 96), (78, 40, 84), (48, 30, 68), (22, 18, 44)], False, 0.30),
    "scene_night": ((6, 10, 32), (30, 52, 104), (214, 226, 255), [(30, 48, 92), (22, 36, 76), (14, 26, 58), (8, 14, 36)], True, 0.75),
    "scene_forest": ((86, 150, 176), (226, 232, 190), (255, 246, 200), [(110, 156, 150), (72, 124, 116), (42, 92, 86), (22, 58, 56)], False, 0.45),
    "scene_ocean": ((20, 70, 128), (140, 214, 224), (255, 240, 200), [(70, 130, 170), (44, 100, 150), (24, 70, 120), (12, 44, 88)], False, 0.55),
    "scene_desert": ((120, 70, 120), (255, 178, 96), (255, 220, 140), [(220, 130, 90), (186, 96, 76), (140, 70, 64), (88, 46, 52)], False, 0.20),
}


def lerp(a, b, t):
    return tuple(a[i] + (b[i] - a[i]) * t for i in range(3))


def render(name, spec, seed):
    top, bottom, sun, ridges, stars, sun_x = spec
    rng = np.random.default_rng(seed)
    y = np.linspace(0, 1, H)[:, None, None]
    sky = np.array(top)[None, None, :] * (1 - y) + np.array(bottom)[None, None, :] * y
    img = Image.fromarray(np.broadcast_to(sky, (H, W, 3)).astype("uint8"))

    glow = Image.new("RGB", (W, H), (0, 0, 0))
    cx, cy = int(W * sun_x), int(H * 0.52)
    gd = ImageDraw.Draw(glow)
    for r, a in ((340, 0.25), (220, 0.45), (120, 0.8)):
        gd.ellipse((cx - r, cy - r, cx + r, cy + r), fill=tuple(int(c * a) for c in sun))
    glow = glow.filter(ImageFilter.GaussianBlur(70))
    img = Image.fromarray(np.clip(np.asarray(img, dtype=np.int32) + np.asarray(glow, dtype=np.int32), 0, 255).astype("uint8"))
    d = ImageDraw.Draw(img)
    d.ellipse((cx - 46, cy - 46, cx + 46, cy + 46), fill=sun)

    if stars:
        for _ in range(260):
            sx, sy = rng.integers(0, W), rng.integers(0, int(H * 0.5))
            s = int(rng.integers(1, 4))
            b = int(rng.integers(150, 255))
            d.ellipse((sx, sy, sx + s, sy + s), fill=(b, b, 255))

    xs = np.arange(W)
    for i, colour in enumerate(ridges):
        base = H * (0.58 + 0.1 * i)
        amp = 120 - 15 * i
        phase = rng.uniform(0, 6.28, 3)
        f = rng.uniform(0.0016, 0.0032, 3) * (1 + i * 0.35)
        ridge = base - amp * (0.5 * np.sin(xs * f[0] + phase[0]) + 0.3 * np.sin(xs * f[1] * 2.3 + phase[1]) + 0.2 * np.sin(xs * f[2] * 4.1 + phase[2]))
        pts = [(0, H)] + [(int(x), int(v)) for x, v in zip(xs, ridge)] + [(W, H)]
        d.polygon(pts, fill=colour)
    img = img.filter(ImageFilter.GaussianBlur(0.6))
    out = RES / "drawable-nodpi" / f"{name}.webp"
    img.save(out, "WEBP", quality=82, method=6)
    print(out.name, out.stat().st_size // 1024, "KB")


def render_video():
    out = RES / "raw/ambient_loop.mp4"
    # Slow drifting gradient + soft pad chord. Seamless enough for a looping ambience.
    chord = "+".join(f"0.07*sin(2*PI*{f}*t)" for f in (110, 164.81, 220, 277.18))
    tremolo = "(0.75+0.25*sin(2*PI*0.12*t))"
    cmd = [
        "ffmpeg", "-y", "-loglevel", "error",
        "-f", "lavfi", "-i", "gradients=s=1280x720:d=24:r=24:speed=0.012:c0=0x1b2a6b:c1=0xe8626a:c2=0x2b9bb5:c3=0x6a3d9a:nb_colors=4",
        "-f", "lavfi", "-i", f"aevalsrc='({chord})*{tremolo}':s=44100:d=24",
        "-vf", "gblur=sigma=12,format=yuv420p",
        "-c:v", "libx264", "-preset", "slow", "-crf", "30", "-profile:v", "main",
        "-c:a", "aac", "-b:a", "96k", "-movflags", "+faststart", "-shortest", str(out),
    ]
    subprocess.run(cmd, check=True)
    print(out.name, out.stat().st_size // 1024, "KB")


if __name__ == "__main__":
    for n, (name, spec) in enumerate(SCENES.items()):
        render(name, spec, seed=7 + n)
    render_video()
