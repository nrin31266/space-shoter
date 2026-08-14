package com.alexei.spaceshooter.utils;

/**
 * Created by Alex on 19/06/2015.
 *
 * All the game sounds by name
 */
public enum SoundName {
    Alarm,
    LaserShoot, LaserShoot2,
    Hit7,
    Explode, Explode2, Explode3, Explode4, Explode5, Explode8, Explode7,
    Rocket, EndGame, Laser, GetDamage, Tick, Ready, Go, Warning,

    // Distinct identity SFX (synthesized, see tools/gen_audio.py)
    PowerUp,    // weapon upgrade pickup
    Pickup,     // currency pickup
    WaveStart,  // wave announcement
    WaveClear,  // wave clear fanfare
    BossWarning,// boss entrance horn

    //music
    Ut, ActionMusic

}
