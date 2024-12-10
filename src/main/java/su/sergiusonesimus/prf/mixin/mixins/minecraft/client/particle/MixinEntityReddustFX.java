package su.sergiusonesimus.prf.mixin.mixins.minecraft.client.particle;

import net.minecraft.client.particle.*;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityReddustFX.class)
public class MixinEntityReddustFX extends MixinEntityFX {

    @Inject(method = "onUpdate", at = @At("TAIL"))
    public void onUpdate(CallbackInfo ci) {
        checkIfBehindTransparent();
    }

}
