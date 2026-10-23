package com.alexei.spaceshooter;

import android.os.Bundle;
import android.os.Build;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useImmersiveMode = true;
		config.useAccelerometer = false;
		config.useCompass = false;
        boolean debug = (getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        int mission = debug ? getIntent().getIntExtra("preview_mission", 0) : 0;
        int encounter = debug ? getIntent().getIntExtra("preview_encounter", 0) : 0;
        WindowManager.LayoutParams window = getWindow().getAttributes();
        window.layoutInDisplayCutoutMode = Build.VERSION.SDK_INT >= 30
                ? WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                : WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        getWindow().setAttributes(window);
        initialize(new MainGame(mission, encounter), config);
	}
}
