package su.sergiusonesimus.prf.mixin.mixins.minecraft.client.particle;

import net.minecraft.client.particle.EntityFireworkSparkFX;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityFireworkSparkFX.class)
public class MixinEntityFireworkSparkFX extends MixinEntityFX {

    @Inject(method = "onUpdate", at = @At("TAIL"))
    public void onUpdate(CallbackInfo ci) {
        checkIfBehindTransparent();
    }

}
