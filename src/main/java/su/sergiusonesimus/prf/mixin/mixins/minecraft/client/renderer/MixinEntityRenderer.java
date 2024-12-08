package su.sergiusonesimus.prf.mixin.mixins.minecraft.client.renderer;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.ForgeHooksClient;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import su.sergiusonesimus.prf.mixin.interfaces.minecraft.client.particle.IMixinEffectRenderer;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Shadow(remap = true)
    private Minecraft mc;

    @Shadow(remap = true)
    private boolean lightmapUpdateNeeded;

    @Shadow(remap = true)
    public static int anaglyphField;

    @Shadow(remap = true)
    public int debugViewDirection;

    @Shadow(remap = true)
    private double cameraZoom;

    // TODO

    @Shadow(remap = true)
    private void renderHand(float p_78476_1_, int p_78476_2_) {};

    @Shadow(remap = true)
    protected void renderRainSnow(float p_78474_1_) {};

    @Shadow(remap = true)
    public void enableLightmap(double p_78463_1_) {};

    @Shadow(remap = true)
    public void disableLightmap(double p_78483_1_) {};

    @Shadow(remap = true)
    private void renderCloudsCheck(RenderGlobal p_82829_1_, float p_82829_2_) {};

    @Shadow(remap = true)
    private void setupFog(int p_78468_1_, float p_78468_2_) {};

    @Shadow(remap = true)
    private void setupCameraTransform(float p_78479_1_, int p_78479_2_) {};

    @Shadow(remap = true)
    private void updateFogColor(float p_78466_1_) {};

    @Shadow(remap = true)
    public void getMouseOver(float p_78473_1_) {};

    @Shadow(remap = true)
    private void updateLightmap(float p_78472_1_) {};

    @Overwrite
    public void renderWorld(float p_78471_1_, long p_78471_2_) {
        this.mc.mcProfiler.startSection("lightTex");

        if (this.lightmapUpdateNeeded) {
            this.updateLightmap(p_78471_1_);
        }

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.5F);

        if (this.mc.renderViewEntity == null) {
            this.mc.renderViewEntity = this.mc.thePlayer;
        }

        this.mc.mcProfiler.endStartSection("pick");
        this.getMouseOver(p_78471_1_);
        EntityLivingBase entitylivingbase = this.mc.renderViewEntity;
        RenderGlobal renderglobal = this.mc.renderGlobal;
        EffectRenderer effectrenderer = this.mc.effectRenderer;
        double d0 = entitylivingbase.lastTickPosX
            + (entitylivingbase.posX - entitylivingbase.lastTickPosX) * (double) p_78471_1_;
        double d1 = entitylivingbase.lastTickPosY
            + (entitylivingbase.posY - entitylivingbase.lastTickPosY) * (double) p_78471_1_;
        double d2 = entitylivingbase.lastTickPosZ
            + (entitylivingbase.posZ - entitylivingbase.lastTickPosZ) * (double) p_78471_1_;
        this.mc.mcProfiler.endStartSection("center");

        for (int j = 0; j < 2; ++j) {
            if (this.mc.gameSettings.anaglyph) {
                anaglyphField = j;

                if (anaglyphField == 0) {
                    GL11.glColorMask(false, true, true, false);
                } else {
                    GL11.glColorMask(true, false, false, false);
                }
            }

            this.mc.mcProfiler.endStartSection("clear");
            GL11.glViewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
            this.updateFogColor(p_78471_1_);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glEnable(GL11.GL_CULL_FACE);
            this.mc.mcProfiler.endStartSection("camera");
            this.setupCameraTransform(p_78471_1_, j);
            ActiveRenderInfo.updateRenderInfo(this.mc.thePlayer, this.mc.gameSettings.thirdPersonView == 2);
            this.mc.mcProfiler.endStartSection("frustrum");
            ClippingHelperImpl.getInstance();

            if (this.mc.gameSettings.renderDistanceChunks >= 4) {
                this.setupFog(-1, p_78471_1_);
                this.mc.mcProfiler.endStartSection("sky");
                renderglobal.renderSky(p_78471_1_);
            }

            GL11.glEnable(GL11.GL_FOG);
            this.setupFog(1, p_78471_1_);

            if (this.mc.gameSettings.ambientOcclusion != 0) {
                GL11.glShadeModel(GL11.GL_SMOOTH);
            }

            this.mc.mcProfiler.endStartSection("culling");
            Frustrum frustrum = new Frustrum();
            frustrum.setPosition(d0, d1, d2);
            this.mc.renderGlobal.clipRenderersByFrustum(frustrum, p_78471_1_);

            if (j == 0) {
                this.mc.mcProfiler.endStartSection("updatechunks");

                while (!this.mc.renderGlobal.updateRenderers(entitylivingbase, false) && p_78471_2_ != 0L) {
                    long k = p_78471_2_ - System.nanoTime();

                    if (k < 0L || k > 1000000000L) {
                        break;
                    }
                }
            }

            if (entitylivingbase.posY < 128.0D) {
                this.renderCloudsCheck(renderglobal, p_78471_1_);
            }

            this.mc.mcProfiler.endStartSection("prepareterrain");
            this.setupFog(0, p_78471_1_);
            GL11.glEnable(GL11.GL_FOG);
            this.mc.getTextureManager()
                .bindTexture(TextureMap.locationBlocksTexture);
            RenderHelper.disableStandardItemLighting();
            this.mc.mcProfiler.endStartSection("terrain");
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPushMatrix();
            renderglobal.sortAndRender(entitylivingbase, 0, (double) p_78471_1_);
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            EntityPlayer entityplayer;

            if (this.debugViewDirection == 0) {
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glPopMatrix();
                GL11.glPushMatrix();
                RenderHelper.enableStandardItemLighting();
                this.mc.mcProfiler.endStartSection("entities");
                net.minecraftforge.client.ForgeHooksClient.setRenderPass(0);
                renderglobal.renderEntities(entitylivingbase, frustrum, p_78471_1_);
                net.minecraftforge.client.ForgeHooksClient.setRenderPass(0);
                // ToDo: Try and figure out how to make particles render sorted correctly.. {They render behind water}
                RenderHelper.disableStandardItemLighting();
                this.disableLightmap((double) p_78471_1_);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glPopMatrix();
                GL11.glPushMatrix();

                if (this.mc.objectMouseOver != null && entitylivingbase.isInsideOfMaterial(Material.water)
                    && entitylivingbase instanceof EntityPlayer
                    && !this.mc.gameSettings.hideGUI) {
                    entityplayer = (EntityPlayer) entitylivingbase;
                    GL11.glDisable(GL11.GL_ALPHA_TEST);
                    this.mc.mcProfiler.endStartSection("outline");
                    if (!ForgeHooksClient.onDrawBlockHighlight(
                        renderglobal,
                        entityplayer,
                        mc.objectMouseOver,
                        0,
                        entityplayer.inventory.getCurrentItem(),
                        p_78471_1_)) {
                        renderglobal.drawSelectionBox(entityplayer, this.mc.objectMouseOver, 0, p_78471_1_);
                    }
                    GL11.glEnable(GL11.GL_ALPHA_TEST);
                }
            }

            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPopMatrix();

            if (this.cameraZoom == 1.0D && entitylivingbase instanceof EntityPlayer
                && !this.mc.gameSettings.hideGUI
                && this.mc.objectMouseOver != null
                && !entitylivingbase.isInsideOfMaterial(Material.water)) {
                entityplayer = (EntityPlayer) entitylivingbase;
                GL11.glDisable(GL11.GL_ALPHA_TEST);
                this.mc.mcProfiler.endStartSection("outline");
                if (!ForgeHooksClient.onDrawBlockHighlight(
                    renderglobal,
                    entityplayer,
                    mc.objectMouseOver,
                    0,
                    entityplayer.inventory.getCurrentItem(),
                    p_78471_1_)) {
                    renderglobal.drawSelectionBox(entityplayer, this.mc.objectMouseOver, 0, p_78471_1_);
                }
                GL11.glEnable(GL11.GL_ALPHA_TEST);
            }

            this.mc.mcProfiler.endStartSection("destroyProgress");
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 1, 1, 0);
            renderglobal.drawBlockDamageTexture(Tessellator.instance, entitylivingbase, p_78471_1_);
            GL11.glDisable(GL11.GL_BLEND);

            if (this.debugViewDirection == 0) {
                this.enableLightmap((double) p_78471_1_);
                this.mc.mcProfiler.endStartSection("litParticles");
                ((IMixinEffectRenderer) effectrenderer).renderLitParticles(entitylivingbase, p_78471_1_, true);
                RenderHelper.disableStandardItemLighting();
                this.setupFog(0, p_78471_1_);
                this.mc.mcProfiler.endStartSection("particles");
                ((IMixinEffectRenderer) effectrenderer).renderParticles(entitylivingbase, p_78471_1_, true);
                this.disableLightmap((double) p_78471_1_);
            }

            GL11.glDepthMask(false);
            GL11.glEnable(GL11.GL_CULL_FACE);
            this.mc.mcProfiler.endStartSection("weather");
            this.renderRainSnow(p_78471_1_);
            GL11.glDepthMask(true);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_CULL_FACE);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            this.setupFog(0, p_78471_1_);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDepthMask(false);
            this.mc.getTextureManager()
                .bindTexture(TextureMap.locationBlocksTexture);

            if (this.mc.gameSettings.fancyGraphics) {
                this.mc.mcProfiler.endStartSection("water");

                if (this.mc.gameSettings.ambientOcclusion != 0) {
                    GL11.glShadeModel(GL11.GL_SMOOTH);
                }

                GL11.glEnable(GL11.GL_BLEND);
                OpenGlHelper.glBlendFunc(770, 771, 1, 0);

                if (this.mc.gameSettings.anaglyph) {
                    if (anaglyphField == 0) {
                        GL11.glColorMask(false, true, true, true);
                    } else {
                        GL11.glColorMask(true, false, false, true);
                    }

                    renderglobal.sortAndRender(entitylivingbase, 1, (double) p_78471_1_);
                } else {
                    renderglobal.sortAndRender(entitylivingbase, 1, (double) p_78471_1_);
                }

                GL11.glDisable(GL11.GL_BLEND);
                GL11.glShadeModel(GL11.GL_FLAT);
            } else {
                this.mc.mcProfiler.endStartSection("water");
                renderglobal.sortAndRender(entitylivingbase, 1, (double) p_78471_1_);
            }

            if (this.debugViewDirection == 0) {
                this.enableLightmap((double) p_78471_1_);
                this.mc.mcProfiler.endStartSection("litParticles");
                ((IMixinEffectRenderer) effectrenderer).renderLitParticles(entitylivingbase, p_78471_1_, false);
                RenderHelper.disableStandardItemLighting();
                this.setupFog(0, p_78471_1_);
                this.mc.mcProfiler.endStartSection("particles");
                ((IMixinEffectRenderer) effectrenderer).renderParticles(entitylivingbase, p_78471_1_, false);
                this.disableLightmap((double) p_78471_1_);
            }

            if (this.debugViewDirection == 0) // Only render if render pass 0 happens as well.
            {
                RenderHelper.enableStandardItemLighting();
                this.mc.mcProfiler.endStartSection("entities");
                ForgeHooksClient.setRenderPass(1);
                renderglobal.renderEntities(entitylivingbase, frustrum, p_78471_1_);
                ForgeHooksClient.setRenderPass(-1);
                RenderHelper.disableStandardItemLighting();
            }

            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_FOG);

            if (entitylivingbase.posY >= 128.0D) {
                this.mc.mcProfiler.endStartSection("aboveClouds");
                this.renderCloudsCheck(renderglobal, p_78471_1_);
            }

            this.mc.mcProfiler.endStartSection("FRenderLast");
            ForgeHooksClient.dispatchRenderLast(renderglobal, p_78471_1_);

            this.mc.mcProfiler.endStartSection("hand");

            if (!ForgeHooksClient.renderFirstPersonHand(renderglobal, p_78471_1_, j) && this.cameraZoom == 1.0D) {
                GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
                this.renderHand(p_78471_1_, j);
            }

            if (!this.mc.gameSettings.anaglyph) {
                this.mc.mcProfiler.endSection();
                return;
            }
        }

        GL11.glColorMask(true, true, true, false);
        this.mc.mcProfiler.endSection();
    }

}
