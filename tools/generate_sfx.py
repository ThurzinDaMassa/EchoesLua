"""Deterministic procedural WAV assets for Echoes Lua."""
import math
import random
import struct
import wave
from pathlib import Path

RATE = 44100
OUT = Path(__file__).resolve().parents[1] / "assets" / "sounds"
random.seed(73021)

def save(name, seconds, sample):
    frames = bytearray()
    for i in range(int(seconds * RATE)):
        t = i / RATE
        value = max(-1.0, min(1.0, sample(t, seconds)))
        frames += struct.pack("<h", int(value * 32767))
    with wave.open(str(OUT / name), "wb") as wav:
        wav.setnchannels(1); wav.setsampwidth(2); wav.setframerate(RATE)
        wav.writeframes(frames)

def fire(t, length):
    env = math.exp(-t * 12) * min(1, t * 90)
    sweep = math.sin(2 * math.pi * (980 - 620 * t) * t)
    plasma = math.sin(2 * math.pi * 1840 * t + 4 * math.sin(2 * math.pi * 90 * t))
    return (sweep * .58 + plasma * .22 + (random.random() * 2 - 1) * .18) * env

def hit(t, length):
    env = math.exp(-t * 15)
    metallic = math.sin(2 * math.pi * 210 * t) + .45 * math.sin(2 * math.pi * 470 * t)
    return (metallic * .43 + (random.random() * 2 - 1) * .30) * env

def medkit(t, length):
    env = min(1, t * 20) * min(1, (length - t) * 8)
    notes = math.sin(2 * math.pi * 659 * t) + .55 * math.sin(2 * math.pi * 988 * t)
    return notes * .28 * env

def damage(t, length):
    attack = min(1, t * 100)
    env = attack * math.exp(-t * 9.5)
    suit_thump = math.sin(2 * math.pi * (118 - 42 * t) * t) * .62
    alarm = math.sin(2 * math.pi * 720 * t) * math.exp(-t * 18) * .22
    grit = (random.random() * 2 - 1) * .20
    return (suit_thump + alarm + grit) * env

def mars(t, length):
    fade = min(1, t * 1.5, (length - t) * 1.5)
    wind = (random.random() * 2 - 1) * .07
    drone = math.sin(2 * math.pi * 47 * t) * .08 + math.sin(2 * math.pi * 71 * t) * .04
    pulse = math.sin(2 * math.pi * .18 * t) * .025
    return (wind + drone + pulse) * fade

save("weapon_fire.wav", .34, fire)
save("enemy_hit.wav", .30, hit)
save("medkit_pickup.wav", .55, medkit)
save("ambient_mars.wav", 8.0, mars)
save("player_damage.wav", .46, damage)
