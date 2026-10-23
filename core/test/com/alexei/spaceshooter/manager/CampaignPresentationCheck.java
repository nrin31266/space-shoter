package com.alexei.spaceshooter.manager;

import com.alexei.spaceshooter.data.CampaignCatalog;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;
import java.security.MessageDigest;

public final class CampaignPresentationCheck {
    public static void main(String[] args) throws Exception {
        FileHandle catalogFile = new FileHandle(args[0]);
        CampaignCatalog catalog = new CampaignCatalog(catalogFile);
        int images = 0, audio = 0;
        String atlas = catalogFile.sibling("art.atlas").readString("UTF-8");
        for (JsonValue file : catalog.root.get("files")) {
            String path = file.asString();
            byte[] bytes = catalogFile.parent().child(path).readBytes();
            StringBuilder hash = new StringBuilder();
            for (byte b : MessageDigest.getInstance("SHA-256").digest(bytes)) hash.append(String.format("%02x", b & 255));
            assert hash.toString().equals(catalog.root.get("sha256").getString(path)) : "Asset changed: " + path;
            if (path.endsWith(".png")) {
                images++;
                assert atlas.contains("\n" + path.substring(4, path.length() - 4) + "\n") : "Unpacked image: " + path;
            }
            if (path.endsWith(".wav")) audio++;
        }
        CampaignWorld world = new CampaignWorld(catalog, new CampaignStore.State(), true, new CampaignWorld.Listener() {
            public void sound(int cue) {}
            public void checkpoint(CampaignStore.State state) { throw new AssertionError("Preview wrote save"); }
        });
        while (world.stage == CampaignWorld.Stage.INTRO) world.update(1f / 60);
        CampaignWorld.Spec rock = world.spec(catalog.record("Prefabs/Enemies/StormAsteroid.prefab"));
        for (int i = 0; i < 4; i++) {
            float scale = new float[]{.7f, 1, 1.5f, 2}[i];
            assert rock.asteroidSprite(scale, 1).equals(rock.asteroidFrames[0][i]);
            assert rock.asteroidSprite(scale, .5f).equals(rock.asteroidFrames[1][i]);
            assert rock.asteroidSprite(scale, .25f).equals(rock.asteroidFrames[2][i]);
        }
        CampaignWorld.Enemy target = new CampaignWorld.Enemy();
        target.spec = world.spec(catalog.record("Prefabs/Enemies/Scout.prefab"));
        target.group = world.encounter.get("Groups").get(0);
        target.scale = 1; target.hp = target.maxHp = 100;
        target.x = world.width * .5f; target.y = world.height * .6f;
        target.motionPaused = true; target.visibleSeconds = 1;
        world.enemies.clear(); world.enemies.add(target);
        for (int branch = 0; branch < 3; branch++) {
            world.bursts.clear(); world.shots.clear();
            CampaignWorld.Shot shot = new CampaignWorld.Shot();
            shot.branch = branch; shot.damage = 1; shot.life = 1;
            shot.x = shot.previousX = target.x;
            shot.y = shot.previousY = target.y - .5f; shot.vy = 60;
            world.shots.add(shot);
            world.update(1f / 60);
            assert world.bursts.size == 1 : "One impact per hit";
            CampaignWorld.Burst burst = world.bursts.first();
            assert burst.frames == (branch == 0 ? world.hitPulseFrames : branch == 1 ? world.hitPlasmaFrames : world.hitHomingFrames);
            assert Math.abs(burst.scale - new float[]{.55f, .8f, 1.05f}[branch]) < .001f;
            assert burst.y < target.y : "Impact belongs on entry edge, not hull center";
            assert Math.abs(burst.duration - .22f) < .001f;
        }
        world.shots.clear();
        boolean testedRestricted = false, testedOpen = false;
        for (int encounter = 0; encounter < 2; encounter++) {
            CampaignStore.State state = new CampaignStore.State(); state.encounter = encounter;
            CampaignWorld boundary = new CampaignWorld(catalog, state, true, new CampaignWorld.Listener() {
                public void sound(int cue) {}
                public void checkpoint(CampaignStore.State save) { throw new AssertionError("Preview wrote save"); }
            });
            for (int tick = 0; tick < 120; tick++) { boundary.player.protection = 10; boundary.update(1f / 60); }
            boundary.held = true; boundary.move(10000, 10000);
            boundary.update(1f / 60);
            boolean restricted = boundary.playableX + boundary.playableWidth < .999f || boundary.playableY + boundary.playableHeight < .999f;
            assert restricted == (boundary.boundaryPressedSeconds > 0) : "Only restricted viewport edges show hint: encounter=" + encounter + " stage=" + boundary.stage + " held=" + boundary.held + " restricted=" + restricted + " timer=" + boundary.boundaryPressedSeconds;
            testedRestricted |= restricted; testedOpen |= !restricted;
            boundary.held = false; boundary.update(1f / 60);
            assert boundary.boundaryPressedSeconds == 0 : "Release resets hint hold";
        }
        assert testedRestricted && testedOpen : "Check both full-screen and formation camera";
        JsonValue support = catalog.component(world.playerSpec.record, "SupportDrone");
        JsonValue visual = CampaignWorld.local(world.playerSpec.record, support.get("visual"));
        assert catalog.root.get("sprites").has(CampaignCatalog.spriteKey(visual.get("m_Sprite"))) : "Support sprite must resolve";
        world.shots.clear();
        world.save.activeWeapon = 2;
        JsonValue pulse = catalog.asset("Data/Weapons/PulseLaser.asset");
        world.launchPattern(pulse, pulse.get("levels").get(0), 0, 0, false, 90);
        assert world.shots.first().branch == 0 : "Support pulse keeps pulse impact while Homing is selected";
        System.out.println("Presentation: 3 impact banks/contact points, support branch, 12 asteroid states; " + images + " PNG packed and " + audio + " WAV unchanged OK");
    }
}
