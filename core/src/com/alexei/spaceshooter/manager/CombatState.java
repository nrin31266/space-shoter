package com.alexei.spaceshooter.manager;

import com.badlogic.gdx.utils.JsonValue;

public class CombatState {
    public final CampaignStore.State save;

    public float heat;
    public float protection;
    public float deathRemaining;
    public float respawnRemaining;
    public float enemyFireGrace;
    public float overdriveRemaining;
    public float droneRemaining;
    public float fireRateMultiplier = 1f;

    public boolean overheated;
    public boolean overheatedJustTriggered;
    public boolean ventJustTriggered;
    public boolean shield;
    public float coolingTimer;

    public CombatState(CampaignStore.State save) {
        CampaignStore.validate(save);
        this.save = save;
    }

    public boolean alive() {
        return save != null && save.lives > 0;
    }

    public boolean canInput() {
        return alive() && deathRemaining <= 0f && respawnRemaining <= 0f;
    }

    public boolean canFire() {
        return canInput() && !overheated;
    }

    public void protect(float seconds) {
        if (seconds > protection) {
            protection = seconds;
        }
    }

    public int hit() {
        if (!alive() || deathRemaining > 0f || respawnRemaining > 0f || protection > 0f) {
            return 0;
        }
        if (shield) {
            shield = false;
            protect(.7f);
            return 1;
        }

        if (save != null) {
            if (save.weaponLevels != null && save.activeWeapon >= 0 && save.activeWeapon < save.weaponLevels.length) {
                if (save.weaponLevels[save.activeWeapon] > 1) {
                    save.weaponLevels[save.activeWeapon]--;
                }
            }
            save.lives--;
        }

        overdriveRemaining = 0f;
        droneRemaining = 0f;
        fireRateMultiplier = 1f;
        heat = 0f;
        overheated = false;
        coolingTimer = 0f;
        deathRemaining = 0.6f;
        respawnRemaining = 0f;

        if (save == null || save.lives <= 0) {
            if (save != null) {
                save.lives = 0;
            }
            return 3;
        }
        return 2;
    }

    public boolean cycle() {
        if (!canInput() || save == null || save.weaponLevels == null || save.weaponLevels.length <= 1) {
            return false;
        }

        int total = save.weaponLevels.length;
        for (int i = 1; i < total; i++) {
            int candidate = (save.activeWeapon + i) % total;
            if (save.weaponLevels[candidate] > 0) {
                save.activeWeapon = candidate;
                return true;
            }
        }
        return false;
    }

    public boolean canUse(JsonValue item) {
        switch (item.getInt("Kind")) {
            case 0: return true;
            case 1: return save.weaponLevels[item.getInt("Branch")] < 5;
            case 2: case 3: case 4: return true;
            case 5: return save.missiles < 3;
            case 6: return save.lives < 5;
            default: throw new IllegalArgumentException("Unknown pickup kind");
        }
    }

    public boolean pickup(JsonValue item) {
        if (!canInput() || !canUse(item)) return false;
        switch (item.getInt("Kind")) {
            case 0:
                save.shard = (int)Math.min(Integer.MAX_VALUE, (long)save.shard + item.getInt("Amount"));
                break;
            case 1:
                save.activeWeapon = item.getInt("Branch");
                save.weaponLevels[save.activeWeapon]++;
                break;
            case 2: shield = true; break;
            case 3:
                overdriveRemaining = item.getFloat("Duration");
                fireRateMultiplier = item.getFloat("FireRateMultiplier");
                break;
            case 4: droneRemaining = item.getFloat("Duration"); break;
            case 5: save.missiles++; break;
            case 6: save.lives++; break;
            default: throw new IllegalArgumentException("Unknown pickup kind");
        }
        return true;
    }

    public void tick(float seconds, boolean held, JsonValue thermal) {
        if (Float.isNaN(seconds) || Float.isInfinite(seconds) || seconds < 0f) {
            throw new IllegalArgumentException("Delta seconds must be finite and >= 0: " + seconds);
        }
        if (seconds == 0f) {
            return;
        }

        if (deathRemaining > 0f) {
            deathRemaining -= seconds;
            if (deathRemaining <= 0f) {
                float remainder = -deathRemaining;
                deathRemaining = 0f;
                if (save != null && save.lives > 0) {
                    respawnRemaining = 0.55f;
                    if (remainder > 0f) {
                        respawnRemaining -= remainder;
                        if (respawnRemaining <= 0f) {
                            float rem2 = -respawnRemaining;
                            respawnRemaining = 0f;
                            protection = Math.max(0f, 2.5f - rem2);
                            enemyFireGrace = Math.max(0f, 1.0f - rem2);
                        }
                    }
                }
            }
            return;
        }

        if (respawnRemaining > 0f) {
            respawnRemaining -= seconds;
            if (respawnRemaining <= 0f) {
                float remainder = -respawnRemaining;
                respawnRemaining = 0f;
                protection = Math.max(0f, 2.5f - remainder);
                enemyFireGrace = Math.max(0f, 1.0f - remainder);
            }
            return;
        }

        if (protection > 0f) {
            protection = Math.max(0f, protection - seconds);
        }
        if (enemyFireGrace > 0f) {
            enemyFireGrace = Math.max(0f, enemyFireGrace - seconds);
        }
        if (overdriveRemaining > 0f) {
            overdriveRemaining = Math.max(0f, overdriveRemaining - seconds);
            if (overdriveRemaining == 0f) {
                fireRateMultiplier = 1f;
            }
        }
        if (droneRemaining > 0f) {
            droneRemaining = Math.max(0f, droneRemaining - seconds);
        }

        float capacity = thermal.getFloat("capacity");
        float heatPerSecond = thermal.getFloat("heatPerSecond");
        float coolingDelay = thermal.getFloat("coolingDelay");
        float coolRate = thermal.getFloat("coolRate");
        float overheatCoolRate = thermal.getFloat("overheatCoolRate");

        if (overheated) {
            heat -= overheatCoolRate * seconds;
            if (heat <= 0f) {
                heat = 0f;
                overheated = false;
                coolingTimer = 0f;
                ventJustTriggered = true;
            }
        } else {
            if (held) {
                coolingTimer = coolingDelay;
                heat += heatPerSecond * seconds;
                if (heat >= capacity) {
                    heat = capacity;
                    overheated = true;
                    overheatedJustTriggered = true;
                }
            } else {
                if (coolingTimer > 0f) {
                    if (seconds <= coolingTimer) {
                        coolingTimer -= seconds;
                    } else {
                        float unused = seconds - coolingTimer;
                        coolingTimer = 0f;
                        heat -= coolRate * unused;
                        if (heat < 0f) {
                            heat = 0f;
                        }
                    }
                } else {
                    heat -= coolRate * seconds;
                    if (heat < 0f) {
                        heat = 0f;
                    }
                }
            }
        }
    }
}
