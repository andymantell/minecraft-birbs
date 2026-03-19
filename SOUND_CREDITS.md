# Sound Credits

All bird call recordings in this mod are sourced from [xeno-canto.org](https://xeno-canto.org) and are licensed under Creative Commons. Each recording is attributed below.

## Robin (Erithacus rubecula)

| Sound File | XC ID | Recordist | Licence | Trim | URL |
|-----------|-------|-----------|---------|------|-----|
| `robin/song1.ogg` | XC527484 | Benoit Van Hecke | CC BY-SA 4.0 | ss=2, t=4 | https://xeno-canto.org/527484 |
| `robin/song2.ogg` | XC470483 | Marie-Lan Tay Pamart | CC BY-SA 4.0 | ss=1, t=4 | https://xeno-canto.org/470483 |
| `robin/call.ogg` | XC239703 | (see xeno-canto) | (see xeno-canto) | ss=0, t=2 | https://xeno-canto.org/239703 |
| `robin/alarm.ogg` | XC380053 | (see xeno-canto) | (see xeno-canto) | ss=0, t=2 | https://xeno-canto.org/380053 |
| `robin/hurt.ogg` | XC380053 | (see xeno-canto) | (see xeno-canto) | ss=0, t=0.5 | https://xeno-canto.org/380053 |
| `robin/death.ogg` | XC527484 | Benoit Van Hecke | CC BY-SA 4.0 | ss=5, t=1 | https://xeno-canto.org/527484 |

## Blue Tit (Cyanistes caeruleus)

| Sound File | XC ID | Recordist | Licence | Trim | URL |
|-----------|-------|-----------|---------|------|-----|
| `blue_tit/song1.ogg` | XC528258 | (see xeno-canto) | (see xeno-canto) | ss=1, t=4 | https://xeno-canto.org/528258 |
| `blue_tit/song2.ogg` | XC66999 | (see xeno-canto) | (see xeno-canto) | ss=0, t=4 | https://xeno-canto.org/66999 |
| `blue_tit/call.ogg` | XC507333 | (see xeno-canto) | (see xeno-canto) | ss=0, t=2 | https://xeno-canto.org/507333 |
| `blue_tit/alarm.ogg` | XC301975 | (see xeno-canto) | (see xeno-canto) | ss=0, t=2 | https://xeno-canto.org/301975 |
| `blue_tit/hurt.ogg` | XC301975 | (see xeno-canto) | (see xeno-canto) | ss=0, t=0.5 | https://xeno-canto.org/301975 |
| `blue_tit/death.ogg` | XC528258 | (see xeno-canto) | (see xeno-canto) | ss=3, t=1 | https://xeno-canto.org/528258 |

## Barn Owl (Tyto alba)

| Sound File | XC ID | Recordist | Licence | Trim | URL |
|-----------|-------|-----------|---------|------|-----|
| `barn_owl/screech.ogg` | XC344553 | (see xeno-canto) | (see xeno-canto) | ss=0, t=3 | https://xeno-canto.org/344553 |
| `barn_owl/hiss.ogg` | XC411768 | (see xeno-canto) | (see xeno-canto) | ss=0, t=2 | https://xeno-canto.org/411768 |
| `barn_owl/chirrup.ogg` | XC291832 | (see xeno-canto) | (see xeno-canto) | ss=0, t=2 | https://xeno-canto.org/291832 |
| `barn_owl/hurt.ogg` | XC344553 | (see xeno-canto) | (see xeno-canto) | ss=1, t=0.5 | https://xeno-canto.org/344553 |
| `barn_owl/death.ogg` | XC344553 | (see xeno-canto) | (see xeno-canto) | ss=2, t=1 | https://xeno-canto.org/344553 |

## Peregrine Falcon (Falco peregrinus)

| Sound File | XC ID | Recordist | Licence | Trim | URL |
|-----------|-------|-----------|---------|------|-----|
| `peregrine_falcon/kak.ogg` | XC432856 | (see xeno-canto) | (see xeno-canto) | ss=0, t=3 | https://xeno-canto.org/432856 |
| `peregrine_falcon/chitter.ogg` | XC363999 | (see xeno-canto) | (see xeno-canto) | ss=0, t=2 | https://xeno-canto.org/363999 |
| `peregrine_falcon/eechip.ogg` | XC363999 | (see xeno-canto) | (see xeno-canto) | ss=3, t=1.5 | https://xeno-canto.org/363999 |
| `peregrine_falcon/hurt.ogg` | XC432856 | (see xeno-canto) | (see xeno-canto) | ss=0.5, t=0.5 | https://xeno-canto.org/432856 |
| `peregrine_falcon/death.ogg` | XC432856 | (see xeno-canto) | (see xeno-canto) | ss=1, t=1 | https://xeno-canto.org/432856 |

## Mallard (Anas platyrhynchos)

| Sound File | XC ID | Recordist | Licence | Trim | URL |
|-----------|-------|-----------|---------|------|-----|
| `mallard/quack.ogg` | XC430724 | (see xeno-canto) | (see xeno-canto) | ss=0, t=3 | https://xeno-canto.org/430724 |
| `mallard/raeb.ogg` | XC353681 | (see xeno-canto) | (see xeno-canto) | ss=0, t=2 | https://xeno-canto.org/353681 |
| `mallard/wing_whistle.ogg` | XC279944 | (see xeno-canto) | (see xeno-canto) | ss=0, t=2 | https://xeno-canto.org/279944 |
| `mallard/hurt.ogg` | XC430724 | (see xeno-canto) | (see xeno-canto) | ss=0, t=0.5 | https://xeno-canto.org/430724 |
| `mallard/death.ogg` | XC430724 | (see xeno-canto) | (see xeno-canto) | ss=1, t=1 | https://xeno-canto.org/430724 |

---

## Processing

All recordings were converted from xeno-canto MP3 to Minecraft .ogg format:
```
ffmpeg -i input.mp3 -ss START -t DURATION -ac 1 -ar 44100 -c:a libvorbis -q:a 4 output.ogg
```

## Note on Licences

Visit each XC URL to verify the specific licence. We targeted CC BY-SA 4.0 recordings where possible. Some may be CC BY-NC-SA 4.0 — check each recording's page for the exact licence before any commercial distribution.
