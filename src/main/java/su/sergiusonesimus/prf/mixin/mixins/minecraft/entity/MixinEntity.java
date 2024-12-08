package su.sergiusonesimus.prf.mixin.mixins.minecraft.entity;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public class MixinEntity {

    @Shadow(remap = true)
    public double posX;

    @Shadow(remap = true)
    public double posY;

    @Shadow(remap = true)
    public double posZ;

    @Shadow(remap = true)
    public World worldObj;

}
