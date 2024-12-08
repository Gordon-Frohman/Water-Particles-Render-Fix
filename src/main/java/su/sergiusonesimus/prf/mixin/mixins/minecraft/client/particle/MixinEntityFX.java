package su.sergiusonesimus.prf.mixin.mixins.minecraft.client.particle;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.util.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import su.sergiusonesimus.prf.mixin.interfaces.minecraft.client.particle.IMixinEntityFX;
import su.sergiusonesimus.prf.mixin.mixins.minecraft.entity.MixinEntity;

@Mixin(EntityFX.class)
public class MixinEntityFX extends MixinEntity implements IMixinEntityFX {

    public boolean isBehindWater = false;

    public boolean getIsBehindWater() {
        return isBehindWater;
    }

    public void setIsBehindWater(boolean isBehindWater) {
        this.isBehindWater = isBehindWater;
    }

    @Inject(method = "onUpdate", at = @At("TAIL"))
    public void onUpdate(CallbackInfo ci) {
        // Raytracing to check if particle is obscured by liquid
        this.isBehindWater = false;
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        double currentX = player.posX;
        double currentY = player.posY + player.eyeHeight;
        double currentZ = player.posZ;
        Vec3 connectingVector = Vec3.createVectorHelper(posX - currentX, posY - currentY, posZ - currentZ);
        double totalDistance = connectingVector.lengthVector();
        double currentDistance = 0;
        Vec3 movementVector = connectingVector.normalize();
        double divider = 0.1;
        movementVector.xCoord *= divider;
        movementVector.yCoord *= divider;
        movementVector.zCoord *= divider;
        int currentBlockX = (int) Math.floor(currentX);
        int currentBlockY = (int) Math.floor(currentY);
        int currentBlockZ = (int) Math.floor(currentZ);
        int prevBlockX = currentBlockX;
        int prevBlockY = currentBlockY;
        int prevBlockZ = currentBlockZ;
        while (currentDistance <= totalDistance) {
            if (currentDistance == 0 || currentBlockX != prevBlockX
                || currentBlockY != prevBlockY
                || currentBlockZ != prevBlockZ) {
                prevBlockX = currentBlockX;
                prevBlockY = currentBlockY;
                prevBlockZ = currentBlockZ;
                Block currentBlock = worldObj.getBlock(currentBlockX, currentBlockY, currentBlockZ);
                if (currentBlock instanceof BlockLiquid) {
                    this.isBehindWater = true;
                    break;
                }
            }
            currentX += movementVector.xCoord;
            currentY += movementVector.yCoord;
            currentZ += movementVector.zCoord;
            currentBlockX = (int) Math.floor(currentX);
            currentBlockY = (int) Math.floor(currentY);
            currentBlockZ = (int) Math.floor(currentZ);
            currentDistance += divider;
        }
    }
}
