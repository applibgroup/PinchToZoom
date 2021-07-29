package com.bogdwellers.pinchtozoom.app;

import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import com.bogdwellers.pinchtozoom.app.slice.SecondAbilitySlice;

/**
 * MainAbility.
 */
public class MainAbility extends Ability {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(SecondAbilitySlice.class.getName());
    }

}
