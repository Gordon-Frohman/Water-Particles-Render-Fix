package su.sergiusonesimus.prf.mixin.mixins.minecraft.client.particle;

import java.util.List;
import java.util.concurrent.Callable;

import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import su.sergiusonesimus.prf.mixin.interfaces.minecraft.client.particle.IMixinEffectRenderer;
import su.sergiusonesimus.prf.mixin.interfaces.minecraft.client.particle.IMixinEntityFX;

@Mixin(EffectRenderer.class)
public class MixinEffectRenderer implements IMixinEffectRenderer {

    @Shadow(remap = true)
    private List[] fxLayers;

    @Shadow(remap = true)
    private TextureManager renderer;

    @Shadow(remap = true)
    private static ResourceLocation particleTextures;

    // TODO

    public void renderParticles(Entity player, float partialTickTime, boolean behindWater) {
        float f1 = ActiveRenderInfo.rotationX;
        float f2 = ActiveRenderInfo.rotationZ;
        float f3 = ActiveRenderInfo.rotationYZ;
        float f4 = ActiveRenderInfo.rotationXY;
        float f5 = ActiveRenderInfo.rotationXZ;
        EntityFX.interpPosX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTickTime;
        EntityFX.interpPosY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTickTime;
        EntityFX.interpPosZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTickTime;

        for (int k = 0; k < 3; ++k) {
            final int i = k;

            if (!this.fxLayers[i].isEmpty()) {
                switch (i) {
                    case 0:
                    default:
                        this.renderer.bindTexture(particleTextures);
                        break;
                    case 1:
                        this.renderer.bindTexture(TextureMap.locationBlocksTexture);
                        break;
                    case 2:
                        this.renderer.bindTexture(TextureMap.locationItemsTexture);
                }

                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glDepthMask(false);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glAlphaFunc(GL11.GL_GREATER, 0.003921569F);
                Tessellator tessellator = Tessellator.instance;
                tessellator.startDrawingQuads();

                for (int j = 0; j < this.fxLayers[i].size(); ++j) {
                    final EntityFX entityfx = (EntityFX) this.fxLayers[i].get(j);
                    if (entityfx == null || ((IMixinEntityFX) entityfx).getIsBehindWater() != behindWater) continue;
                    tessellator.setBrightness(entityfx.getBrightnessForRender(partialTickTime));

                    try {
                        entityfx.renderParticle(tessellator, partialTickTime, f1, f5, f2, f3, f4);
                    } catch (Throwable throwable) {
                        CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Particle");
                        CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being rendered");
                        crashreportcategory.addCrashSectionCallable("Particle", new Callable() {

                            private static final String __OBFID = "CL_00000918";

                            public String call() {
                                return entityfx.toString();
                            }
                        });
                        crashreportcategory.addCrashSectionCallable("Particle Type", new Callable() {

                            private static final String __OBFID = "CL_00000919";

                            public String call() {
                                return i == 0 ? "MISC_TEXTURE"
                                    : (i == 1 ? "TERRAIN_TEXTURE"
                                        : (i == 2 ? "ITEM_TEXTURE"
                                            : (i == 3 ? "ENTITY_PARTICLE_TEXTURE" : "Unknown - " + i)));
                            }
                        });
                        throw new ReportedException(crashreport);
                    }
                }

                tessellator.draw();
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glDepthMask(true);
                GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            }
        }
    }

    public void renderLitParticles(Entity player, float partialTickTime, boolean behindWater) {
        float f1 = 0.017453292F;
        float f2 = MathHelper.cos(player.rotationYaw * 0.017453292F);
        float f3 = MathHelper.sin(player.rotationYaw * 0.017453292F);
        float f4 = -f3 * MathHelper.sin(player.rotationPitch * 0.017453292F);
        float f5 = f2 * MathHelper.sin(player.rotationPitch * 0.017453292F);
        float f6 = MathHelper.cos(player.rotationPitch * 0.017453292F);
        byte b0 = 3;
        List list = this.fxLayers[b0];

        if (!list.isEmpty()) {
            Tessellator tessellator = Tessellator.instance;

            for (int i = 0; i < list.size(); ++i) {
                EntityFX entityfx = (EntityFX) list.get(i);
                if (entityfx == null || ((IMixinEntityFX) entityfx).getIsBehindWater() != behindWater) continue;
                tessellator.setBrightness(entityfx.getBrightnessForRender(partialTickTime));
                entityfx.renderParticle(tessellator, partialTickTime, f2, f6, f3, f4, f5);
            }
        }
    }
}
