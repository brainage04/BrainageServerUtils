package com.github.brainage04.brainageserverutils.mixin.item;

import net.minecraft.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(TridentItem.class)
public abstract class MixinTridentItem {
    @ModifyConstant(
            method = "onStoppedUsing",
            constant = @Constant(intValue = 10)
    )
    private int modifyMinDrawDuration(int original) {
        return TridentItem.MIN_DRAW_DURATION;
    }
}