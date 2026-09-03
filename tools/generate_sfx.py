"""Gera efeitos curtos originais usados pelas interacoes do jogo."""

from __future__ import annotations

import math
import random
import wave
from pathlib import Path

RATE = 44_100
OUT = Path(__file__).resolve().parents[1] / "assets" / "sounds"


def save(name: str, seconds: float, sample_fn) -> None:
    frames = bytearray()
    total = int(RATE * seconds)
    for index in range(total):
        time = index / RATE
        value = max(-1.0, min(1.0, sample_fn(time, index, total)))
        sample = int(value * 32767)
        frames.extend(sample.to_bytes(2, "little", signed=True))
    with wave.open(str(OUT / name), "wb") as output:
        output.setnchannels(1)
        output.setsampwidth(2)
        output.setframerate(RATE)
        output.writeframes(frames)


random.seed(73021)


def noise() -> float:
    return random.uniform(-1.0, 1.0)


def fire(t: float, _i: int, _n: int) -> float:
    envelope = math.exp(-t * 12) * min(1, t * 90)
    sweep = math.sin(2 * math.pi * (980 - 620 * t) * t)
    plasma = math.sin(2 * math.pi * 1840 * t + 4 * math.sin(2 * math.pi * 90 * t))
    return (sweep * 0.58 + plasma * 0.22 + noise() * 0.18) * envelope


def hit(t: float, _i: int, _n: int) -> float:
    envelope = math.exp(-t * 15)
    metallic = math.sin(2 * math.pi * 210 * t) + 0.45 * math.sin(2 * math.pi * 470 * t)
    return (metallic * 0.43 + noise() * 0.30) * envelope


def medkit(t: float, _i: int, total: int) -> float:
    length = total / RATE
    envelope = min(1, t * 20) * min(1, (length - t) * 8)
    notes = math.sin(2 * math.pi * 659 * t) + 0.55 * math.sin(2 * math.pi * 988 * t)
    return notes * 0.28 * envelope


def damage(t: float, _i: int, _n: int) -> float:
    attack = min(1, t * 100)
    envelope = attack * math.exp(-t * 9.5)
    suit_thump = math.sin(2 * math.pi * (118 - 42 * t) * t) * 0.62
    alarm = math.sin(2 * math.pi * 720 * t) * math.exp(-t * 18) * 0.22
    return (suit_thump + alarm + noise() * 0.20) * envelope


def mars(t: float, _i: int, total: int) -> float:
    length = total / RATE
    fade = min(1, t * 1.5, (length - t) * 1.5)
    wind = noise() * 0.07
    drone = math.sin(2 * math.pi * 47 * t) * 0.08 + math.sin(2 * math.pi * 71 * t) * 0.04
    pulse = math.sin(2 * math.pi * 0.18 * t) * 0.025
    return (wind + drone + pulse) * fade


def reload_start(t: float, _i: int, _n: int) -> float:
    click_a = noise() * math.exp(-55 * t)
    click_b = noise() * math.exp(-70 * max(0.0, t - 0.16)) if t >= 0.16 else 0.0
    mechanism = math.sin(2 * math.pi * (220 - 90 * t) * t) * math.exp(-7 * t)
    return 0.38 * click_a + 0.26 * click_b + 0.18 * mechanism


def reload_complete(t: float, _i: int, _n: int) -> float:
    latch = noise() * math.exp(-48 * t)
    ring = math.sin(2 * math.pi * 780 * t) * math.exp(-16 * t)
    confirm = math.sin(2 * math.pi * 1180 * max(0, t - 0.10)) * math.exp(-24 * max(0, t - 0.10))
    return 0.30 * latch + 0.24 * ring + (0.16 * confirm if t >= 0.10 else 0.0)


def chest_open(t: float, _i: int, _n: int) -> float:
    servo = math.sin(2 * math.pi * (95 + 170 * t) * t) * math.exp(-2.8 * t)
    motor = noise() * 0.12 * math.sin(math.pi * min(1, t / 0.48))
    latch = noise() * math.exp(-60 * max(0, t - 0.48)) if t >= 0.48 else 0.0
    return 0.30 * servo + motor + 0.30 * latch


def item_move(t: float, _i: int, _n: int) -> float:
    envelope = math.exp(-22 * t)
    return envelope * (0.24 * math.sin(2 * math.pi * 920 * t)
                       + 0.14 * math.sin(2 * math.pi * 1380 * t))


def enemy_alert(t: float, _i: int, _n: int) -> float:
    pulse = (1.0 if int(t * 7) % 2 == 0 else 0.35)
    sweep = math.sin(2 * math.pi * (310 + 250 * t) * t)
    low = math.sin(2 * math.pi * 82 * t)
    return (0.24 * sweep * pulse + 0.11 * low) * math.exp(-1.5 * t)


def titan(t: float, _i: int, total: int) -> float:
    length = total / RATE
    fade = min(1, t * 1.2, (length - t) * 1.2)
    methane_wind = noise() * 0.055
    deep_atmosphere = math.sin(2 * math.pi * 34 * t) * 0.10
    ice_resonance = math.sin(2 * math.pi * 91 * t + math.sin(t * 0.7) * 2.4) * 0.035
    distant_pulse = math.sin(2 * math.pi * 0.13 * t) * 0.025
    return (methane_wind + deep_atmosphere + ice_resonance + distant_pulse) * fade


OUT.mkdir(parents=True, exist_ok=True)
save("weapon_fire.wav", 0.34, fire)
save("enemy_hit.wav", 0.30, hit)
save("medkit_pickup.wav", 0.55, medkit)
save("ambient_mars.wav", 8.0, mars)
save("player_damage.wav", 0.46, damage)
save("weapon_reload_start.wav", 0.34, reload_start)
save("weapon_reload_complete.wav", 0.28, reload_complete)
save("storage_chest_open.wav", 0.65, chest_open)
save("inventory_move.wav", 0.16, item_move)
save("enemy_alert.wav", 0.72, enemy_alert)
save("ambient_titan.wav", 10.0, titan)
