package su.sergiusonesimus.prf.mixin.mixins.minecraft.client.particle;

import net.minecraft.client.particle.*;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityNoteFX.class)
public class MixinEntityNoteFX extends MixinEntityFX {

    @Inject(method = "onUpdate", at = @At("TAIL"))
    public void onUpdate(CallbackInfo ci) {
        checkIfBehindTransparent();
    }

}
