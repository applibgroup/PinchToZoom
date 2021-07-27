package com.bogdwellers.pinchtozoom.app;

import com.bogdwellers.pinchtozoom.app.slice.MainAbilitySlice;
import com.bogdwellers.pinchtozoom.app.slice.SecondAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;

public class MainAbility extends Ability {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(SecondAbilitySlice.class.getName());
    }

}
