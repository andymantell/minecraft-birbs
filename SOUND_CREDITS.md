# Sound Credits

All sound files used in this mod are documented here with their sources and licences.

## Robin (Erithacus rubecula)

### Current Status: Placeholder Sounds

The following sound files are currently **placeholders** (silent/generated .ogg files) and need to be replaced with real recordings:

| Sound File | Purpose | Status |
|-----------|---------|--------|
| `robin/song1.ogg` | Song variant 1 | Placeholder |
| `robin/song2.ogg` | Song variant 2 | Placeholder |
| `robin/call.ogg` | Contact call | Placeholder |
| `robin/alarm.ogg` | Alarm call | Placeholder |
| `robin/hurt.ogg` | Hurt sound | Placeholder |
| `robin/death.ogg` | Death sound | Placeholder |

### Candidate xeno-canto Recordings (BY-SA licensed)

The following xeno-canto recordings have been identified as suitable replacements (CC BY-SA 4.0):

| XC ID | Recordist | Type | Country | URL |
|-------|-----------|------|---------|-----|
| XC527484 | Benoit Van Hecke | Song | France | https://xeno-canto.org/527484 |
| XC470483 | Marie-Lan Tay Pamart | Song | France | https://xeno-canto.org/470483 |

### Conversion Instructions

To convert xeno-canto MP3 downloads to Minecraft-compatible OGG:

```bash
ffmpeg -i input.mp3 -ss START -t DURATION -ac 1 -ar 44100 -c:a libvorbis -q:a 4 output.ogg
```

Parameters:
- `-ac 1` : mono channel
- `-ar 44100` : 44.1kHz sample rate
- `-c:a libvorbis` : Vorbis codec
- `-q:a 4` : quality level 4

### Licence Requirements

All xeno-canto recordings used must be **CC BY-SA 4.0** licensed. BY-NC-SA recordings are not suitable for redistribution in a mod. Attribution must be maintained in this file and in `docs/ASSET_PROVENANCE.md`.
