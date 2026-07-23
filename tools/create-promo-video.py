"""Build the 30-second vertical Tiny Vow promo video from approved assets."""

from __future__ import annotations

import argparse
import math
import shutil
import subprocess
import wave
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageEnhance, ImageFilter, ImageFont


WIDTH = 1080
HEIGHT = 1920
FPS = 30
SAMPLE_RATE = 48_000
OVERLAP_SECONDS = 0.45
SCENE_DURATIONS = [2.8, 4.0, 4.0, 4.0, 4.0, 4.0, 4.0, 4.0, 3.0]


def run(command: list[str]) -> None:
    subprocess.run(command, check=True)


def font(path: Path, size: int) -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(str(path), size=size)


def centered_text(
    draw: ImageDraw.ImageDraw,
    xy: tuple[int, int],
    text: str,
    selected_font: ImageFont.FreeTypeFont,
    fill: str,
    spacing: int = 8,
) -> None:
    box = draw.multiline_textbbox((0, 0), text, font=selected_font, spacing=spacing, align="center")
    x = xy[0] - (box[2] - box[0]) / 2
    y = xy[1] - (box[3] - box[1]) / 2
    draw.multiline_text((x, y), text, font=selected_font, fill=fill, spacing=spacing, align="center")


def paper_canvas(background_path: Path) -> Image.Image:
    image = Image.open(background_path).convert("RGB").resize((WIDTH, HEIGHT), Image.Resampling.LANCZOS)
    image = ImageEnhance.Brightness(image).enhance(1.025)
    veil = Image.new("RGBA", image.size, (249, 246, 232, 32))
    return Image.alpha_composite(image.convert("RGBA"), veil)


def make_intro(background: Path, icon_path: Path, output: Path, regular: Path, bold: Path) -> None:
    image = paper_canvas(background)
    glow = Image.new("RGBA", image.size, (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(glow)
    glow_draw.ellipse((140, 265, 940, 1065), fill=(224, 236, 209, 105))
    glow = glow.filter(ImageFilter.GaussianBlur(95))
    image = Image.alpha_composite(image, glow)

    icon = Image.open(icon_path).convert("RGBA").resize((292, 292), Image.Resampling.LANCZOS)
    shadow = Image.new("RGBA", image.size, (0, 0, 0, 0))
    shadow.paste((31, 52, 42, 48), (394, 413, 686, 705))
    shadow = shadow.filter(ImageFilter.GaussianBlur(28))
    image = Image.alpha_composite(image, shadow)
    image.alpha_composite(icon, (394, 380))

    draw = ImageDraw.Draw(image)
    centered_text(draw, (WIDTH // 2, 830), "小约定", font(bold, 116), "#23392F")
    centered_text(draw, (WIDTH // 2, 960), "和手机好好相处", font(bold, 58), "#506459")
    centered_text(draw, (WIDTH // 2, 1105), "约定  ·  投入  ·  专注  ·  战报", font(regular, 38), "#6E7B72")
    draw.rounded_rectangle((250, 1238, 830, 1330), radius=46, fill="#E6ECDD", outline="#CDD8C5", width=2)
    centered_text(draw, (WIDTH // 2, 1283), "本地优先的数字习惯陪伴", font(regular, 32), "#43584C")
    image.convert("RGB").save(output, quality=96)


def make_outro(
    background: Path,
    icon_path: Path,
    qr_path: Path,
    output: Path,
    regular: Path,
    bold: Path,
) -> None:
    image = paper_canvas(background)
    draw = ImageDraw.Draw(image)
    centered_text(draw, (WIDTH // 2, 300), "把时间，留给真正想投入的生活", font(bold, 54), "#23392F")
    centered_text(draw, (WIDTH // 2, 405), "不用苛责自己，也能一点点把时间拿回来", font(regular, 34), "#66766C")

    qr = Image.open(qr_path).convert("RGB").resize((400, 400), Image.Resampling.NEAREST)
    qr_card = Image.new("RGBA", (472, 472), "#FFFEF7")
    qr_card_draw = ImageDraw.Draw(qr_card)
    qr_card_draw.rounded_rectangle((1, 1, 470, 470), radius=42, outline="#CFD8C8", width=3)
    qr_card.alpha_composite(qr.convert("RGBA"), (36, 36))
    image.alpha_composite(qr_card, (304, 605))

    icon = Image.open(icon_path).convert("RGBA").resize((150, 150), Image.Resampling.LANCZOS)
    image.alpha_composite(icon, (465, 1195))
    draw = ImageDraw.Draw(image)
    centered_text(draw, (WIDTH // 2, 1430), "小约定  ·  Tiny Vow", font(bold, 48), "#23392F")
    centered_text(draw, (WIDTH // 2, 1515), "tinyvow.rorolo.com", font(regular, 36), "#51645A")
    centered_text(draw, (WIDTH // 2, 1625), "揉揉喽出品  ·  by Rorolo", font(regular, 28), "#7D887F")
    image.convert("RGB").save(output, quality=96)


def create_music(output: Path, seconds: float) -> None:
    sample_count = int(seconds * SAMPLE_RATE)
    t = np.arange(sample_count, dtype=np.float64) / SAMPLE_RATE
    audio = np.zeros(sample_count, dtype=np.float64)
    chords = [
        (130.81, 164.81, 196.00, 246.94),
        (110.00, 130.81, 164.81, 220.00),
        (110.00, 130.81, 164.81, 196.00),
        (98.00, 123.47, 146.83, 196.00),
    ]
    segment = seconds / len(chords)
    for chord_index, chord in enumerate(chords):
        start = int(chord_index * segment * SAMPLE_RATE)
        end = int(min(seconds, (chord_index + 1) * segment) * SAMPLE_RATE)
        local_t = np.arange(end - start, dtype=np.float64) / SAMPLE_RATE
        envelope = np.sin(np.linspace(0, math.pi, end - start)) ** 0.7
        pad = np.zeros(end - start, dtype=np.float64)
        for note_index, frequency in enumerate(chord):
            pad += np.sin(2 * math.pi * frequency * local_t + note_index * 0.35)
            pad += 0.16 * np.sin(2 * math.pi * frequency * 2 * local_t)
        audio[start:end] += 0.055 * envelope * pad / len(chord)

    for beat in np.arange(1.0, seconds, 2.0):
        start = int(beat * SAMPLE_RATE)
        end = min(sample_count, start + int(1.35 * SAMPLE_RATE))
        local_t = np.arange(end - start, dtype=np.float64) / SAMPLE_RATE
        bell = np.sin(2 * math.pi * 523.25 * local_t) + 0.35 * np.sin(2 * math.pi * 784.88 * local_t)
        audio[start:end] += 0.018 * bell * np.exp(-3.0 * local_t)

    fade_samples = int(1.3 * SAMPLE_RATE)
    audio[:fade_samples] *= np.linspace(0, 1, fade_samples)
    audio[-fade_samples:] *= np.linspace(1, 0, fade_samples)
    peak = max(0.001, float(np.max(np.abs(audio))))
    pcm = np.int16(np.clip(audio / peak * 0.42, -1, 1) * 32767)
    stereo = np.column_stack((pcm, pcm)).ravel()
    with wave.open(str(output), "wb") as wav:
        wav.setnchannels(2)
        wav.setsampwidth(2)
        wav.setframerate(SAMPLE_RATE)
        wav.writeframes(stereo.tobytes())


def create_scene(ffmpeg: Path, source: Path, output: Path, duration: float, scene_index: int) -> None:
    frames = round(duration * FPS)
    zoom_delta = 0.00011 if scene_index % 2 == 0 else 0.00008
    vf = (
        f"scale={WIDTH}:{HEIGHT}:force_original_aspect_ratio=increase,"
        f"crop={WIDTH}:{HEIGHT},"
        f"zoompan=z='min(zoom+{zoom_delta},1.022)':"
        f"x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':"
        f"d={frames}:s={WIDTH}x{HEIGHT}:fps={FPS},format=yuv420p"
    )
    run([
        str(ffmpeg), "-y", "-hide_banner", "-loglevel", "error",
        "-loop", "1", "-i", str(source), "-vf", vf,
        "-frames:v", str(frames), "-an", "-c:v", "libx264",
        "-preset", "veryfast", "-crf", "18", "-pix_fmt", "yuv420p", str(output),
    ])


def assemble(ffmpeg: Path, scenes: list[Path], music: Path, output: Path) -> None:
    command = [str(ffmpeg), "-y", "-hide_banner", "-loglevel", "error"]
    for scene in scenes:
        command.extend(["-i", str(scene)])
    command.extend(["-i", str(music)])

    filters: list[str] = []
    current = "0:v"
    elapsed = SCENE_DURATIONS[0]
    for index in range(1, len(scenes)):
        output_label = f"v{index}"
        offset = elapsed - OVERLAP_SECONDS
        filters.append(
            f"[{current}][{index}:v]xfade=transition=fade:duration={OVERLAP_SECONDS}:offset={offset:.2f}[{output_label}]"
        )
        current = output_label
        elapsed += SCENE_DURATIONS[index] - OVERLAP_SECONDS

    filters.append(f"[{len(scenes)}:a]volume=0.32,afade=t=in:st=0:d=1.0,afade=t=out:st={elapsed - 1.3:.2f}:d=1.3[aout]")
    command.extend([
        "-filter_complex", ";".join(filters),
        "-map", f"[{current}]", "-map", "[aout]", "-t", f"{elapsed:.2f}",
        "-c:v", "libx264", "-preset", "medium", "-crf", "18", "-profile:v", "high",
        "-pix_fmt", "yuv420p", "-movflags", "+faststart", "-c:a", "aac", "-b:a", "160k",
        str(output),
    ])
    run(command)


def storyboard(sources: list[Path], output: Path, regular: Path) -> None:
    thumb_w, thumb_h = 270, 480
    canvas = Image.new("RGB", (thumb_w * 3, (thumb_h + 42) * 3), "#F2EFE4")
    draw = ImageDraw.Draw(canvas)
    labels = ["片头", "首页", "约定与投入", "温和阻断", "离线专注", "使用追踪", "奖励与成就", "本地特性", "片尾"]
    for index, (source, label) in enumerate(zip(sources, labels)):
        image = Image.open(source).convert("RGB").resize((thumb_w, thumb_h), Image.Resampling.LANCZOS)
        x = (index % 3) * thumb_w
        y = (index // 3) * (thumb_h + 42)
        canvas.paste(image, (x, y))
        box = draw.textbbox((0, 0), label, font=font(regular, 22))
        draw.text((x + (thumb_w - box[2]) / 2, y + thumb_h + 8), label, font=font(regular, 22), fill="#33473D")
    canvas.save(output, quality=94)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--ffmpeg", required=True, type=Path)
    parser.add_argument("--repo", type=Path, default=Path(__file__).resolve().parents[1])
    args = parser.parse_args()

    repo = args.repo.resolve()
    output_dir = repo / "design" / "promo-video"
    work_dir = output_dir / "work"
    output_dir.mkdir(parents=True, exist_ok=True)
    if work_dir.exists():
        shutil.rmtree(work_dir)
    work_dir.mkdir(parents=True)

    background = repo / "design" / "appstore" / "generated" / "bg-paper-olive-portrait-v1.png"
    icon_path = repo / "design" / "app-icon.png"
    qr_path = repo / "design" / "tinyvow-website-qr.png"
    regular = Path(r"C:\Windows\Fonts\msyh.ttc")
    bold = Path(r"C:\Windows\Fonts\msyhbd.ttc")

    intro = work_dir / "scene_00_intro.png"
    outro = work_dir / "scene_08_outro.png"
    make_intro(background, icon_path, intro, regular, bold)
    make_outro(background, icon_path, qr_path, outro, regular, bold)

    exports = repo / "design" / "appstore" / "exports" / "cn-stores"
    sources = [
        intro,
        exports / "cn-store_01_home_zh-CN_1080x1920_v7.png",
        exports / "cn-store_02_control_encourage_zh-CN_1080x1920_v2.png",
        exports / "cn-store_03_block_overlay_zh-CN_1080x1920_v1.png",
        exports / "cn-store_04_offline_focus_zh-CN_1080x1920_v4.png",
        exports / "cn-store_05_phone_usage_tracking_zh-CN_1080x1920_v3.png",
        exports / "cn-store_06_rewards_achievements_zh-CN_1080x1920_v3.png",
        exports / "cn-store_07_product_characteristics_zh-CN_1080x1920_v2.png",
        outro,
    ]
    missing = [str(path) for path in sources if not path.exists()]
    if missing:
        raise FileNotFoundError("Missing approved assets:\n" + "\n".join(missing))

    scene_files: list[Path] = []
    for index, (source, duration) in enumerate(zip(sources, SCENE_DURATIONS)):
        scene = work_dir / f"scene_{index:02d}.mp4"
        create_scene(args.ffmpeg, source, scene, duration, index)
        scene_files.append(scene)

    total_duration = sum(SCENE_DURATIONS) - OVERLAP_SECONDS * (len(SCENE_DURATIONS) - 1)
    music = work_dir / "tiny-vow-original-ambient.wav"
    create_music(music, total_duration)
    output = output_dir / "tiny-vow-promo-30s-zh.mp4"
    assemble(args.ffmpeg, scene_files, music, output)
    storyboard(sources, output_dir / "tiny-vow-promo-storyboard.jpg", regular)
    shutil.rmtree(work_dir)
    print(output)


if __name__ == "__main__":
    main()
