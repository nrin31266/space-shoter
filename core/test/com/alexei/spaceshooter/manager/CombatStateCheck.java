package com.alexei.spaceshooter.manager;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public final class CombatStateCheck {
    public static void main(String[] args) {
        JsonReader r = new JsonReader();
        JsonValue thermal = r.parse("{capacity:100,heatPerSecond:20,coolingDelay:0.3,coolRate:18,overheatCoolRate:65}");
        CampaignStore.State save = new CampaignStore.State();
        CombatState state = new CombatState(save);
        assert state.hit() == 2 && save.lives == 2;
        assert state.hit() == 0 && save.lives == 2 : "Multiple overlaps must lose only one life";
        state.tick(.6f, false, thermal);
        assert !state.canInput();
        state.tick(.55f, false, thermal);
        assert state.canInput() && state.protection > 2.4f && state.hit() == 0;
        state.tick(3, false, thermal);
        state.shield = true;
        assert state.hit() == 1 && state.hit() == 0 && save.lives == 2;
        state.tick(1, false, thermal);
        assert state.pickup(r.parse("{Kind:1,Branch:1}"));
        assert save.activeWeapon == 1 && save.weaponLevels[1] == 1 && save.weaponLevels[0] == 1;
        save.weaponLevels[1] = 5;
        assert !state.pickup(r.parse("{Kind:1,Branch:1}"));
        state.tick(5, true, thermal);
        assert state.overheated && !state.canFire();
        state.tick(1, true, thermal);
        assert state.overheated && Math.abs(state.heat - 35) < .01f;
        state.tick(1, true, thermal);
        assert !state.overheated && state.heat == 0;
        state.tick(1, true, thermal);
        state.tick(.4f, false, thermal);
        assert Math.abs(state.heat - 18.2f) < .001f : "Cooling uses only remainder after delay";
        CampaignWorld.Shot shot = new CampaignWorld.Shot();
        shot.previousX = 0; shot.previousY = 2; shot.x = 10; shot.y = 2;
        assert CampaignWorld.segmentHit(shot, 5, 2, .1f, .1f) : "Fast shot must not tunnel";
        assert !CampaignWorld.segmentHit(shot, 5, 3, .1f, .1f);
        System.out.println("Lives, shield, branch levels, shared heat, swept collision: OK");
    }
}
