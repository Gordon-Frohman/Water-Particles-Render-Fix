package su.sergiusonesimus.prf.mixin.interfaces.minecraft.client.particle;

import net.minecraft.entity.Entity;

public interface IMixinEffectRenderer {

    public void renderParticles(Entity player, float partialTickTime, boolean behindWater);

    public void renderLitParticles(Entity player, float partialTickTime, boolean behindWater);

}
