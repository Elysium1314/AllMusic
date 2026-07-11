package com.coloryr.allmusic.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;

import com.coloryr.allmusic.client.core.AllMusicBridge;
import com.coloryr.allmusic.client.core.AllMusicCore;
import com.coloryr.allmusic.client.core.render.PictureFrameBuffer;
import com.coloryr.allmusic.client.core.render.TextFrameBuffer;
import com.coloryr.allmusic.client.core.render.TextureRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent17;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import paulscode.sound.Channel;
import paulscode.sound.Library;
import paulscode.sound.SoundSystem;
import paulscode.sound.libraries.ChannelLWJGLOpenAL;

@Mod(modid = "allmusic_client", version = "3.1.1", name = "AllMusic_Client", acceptedMinecraftVersions = "[1.7.10]")
@SideOnly(Side.CLIENT)
public class AllMusic implements AllMusicBridge {
    public static final Logger LOGGER = LogManager.getLogger("AllMusic Client");
    public static SoundSystem sound;

    public static void runMain(Runnable runnable) {
        FMLClientHandler.instance()
                .getClient()
                .func_152344_a(runnable);
    }

    public void sendMessage(String data) {
        data = "[AllMusic Client]" + data;
        LOGGER.warn(data);
        String finalData = data;
        FMLClientHandler.instance()
                .getClient()
                .func_152344_a(
                        () -> FMLClientHandler.instance()
                                .getClient().ingameGUI.getChatGUI()
                                .addToSentMessages(finalData));
    }

    @Mod.EventHandler
    public void test(final FMLLoadCompleteEvent event) {
        Minecraft.getMinecraft().getSoundHandler();

        Library library = ((IGetSoundHandler) sound).allMusic_Client$getSoundLibrary();
        IGetSound sound1 = (IGetSound) library;
        List<Channel> list = sound1.allMusic_Client$getStreamingChannels();
        ChannelLWJGLOpenAL channel = (ChannelLWJGLOpenAL) list.get(list.size() - 1);
        AllMusicCore.init(new File("config").toPath(), this, channel.ALSource);
        AllMusicCore.renderInit();
    }

    @Mod.EventHandler
    public void preload(final FMLPreInitializationEvent evt) {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance()
                .bus()
                .register(this);
        NetworkRegistry.INSTANCE.newEventDrivenChannel("allmusic:channel")
                .register(this);
    }

    @SubscribeEvent
    public void onSound(final PlaySoundEvent17 e) {
        if (!AllMusicCore.isPlay()) return;
        SoundCategory data = e.category;
        if (data == null) return;
        switch (data) {
            case MUSIC:
            case RECORDS:
                new Thread(() -> {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    FMLClientHandler.instance()
                            .getClient()
                            .func_152344_a(() -> {
                                e.manager.stopSound(e.sound);
                            });
                }).start();
        }
    }

    @SubscribeEvent
    public void onServerQuit(final FMLNetworkEvent.ClientDisconnectionFromServerEvent e) {
        AllMusicCore.onServerQuit();
    }

    public int getScreenWidth() {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledresolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        return scaledresolution.getScaledWidth();
    }

    public int getScreenHeight() {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledresolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        return scaledresolution.getScaledHeight();
    }

    public int getTextWidth(String item) {
        return Minecraft.getMinecraft().fontRenderer.getStringWidth(item);
    }

    public int getFontHeight() {
        return Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
    }

    @Override
    public void stopPlayMusic() {
        Minecraft.getMinecraft().getSoundHandler().stopSounds();
        Minecraft.getMinecraft().getSoundHandler().stopSounds();
    }

    @Override
    public TextFrameBuffer makeTextRender(String name) {
        return new CoreRenderTarget();
    }

    @Override
    public TextureRender makeTextureRender(String file) {
        return new TexRender(file);
    }

    @Override
    public PictureFrameBuffer makePictureRender(int size) {
        return new PicRender(size);
    }

    @Override
    public String readText(String file) {
        try {
            IResource resource = Minecraft.getMinecraft().getResourceManager()
                    .getResource(new ResourceLocation("allmusic_client", file));
            InputStream inputStream = resource.getInputStream();
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            inputStream.close();
            return result.toString("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public InputStream readFile(String file) {
        try {
            IResource resource = Minecraft.getMinecraft().getResourceManager()
                    .getResource(new ResourceLocation("allmusic_client", file));
            return resource.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientPacket(final FMLNetworkEvent.ClientCustomPacketEvent evt) {
        try {
            AllMusicCore.packRead(evt.packet.payload());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRenderOverlay(final RenderGameOverlayEvent.Pre e) {
        if (e.type == RenderGameOverlayEvent.ElementType.PORTAL) {
            AllMusicCore.hudUpdate();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onTick(final TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            AllMusicCore.tick();
        }
    }

    public float getVolume() {
        return Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.RECORDS);
    }

    public void drawPic(Object textureID, int size, int x, int y, int ang) {
        int a = size / 2;

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, (int) textureID);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        // GL11.glEnable(GL11.GL_ALPHA);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + a, (float) y + a, 0.0f);

        if (ang > 0) {
            GL11.glRotatef(ang, 0, 0, 1f);
        }

        int x0 = -a;
        int x1 = a;
        int y0 = -a;
        int y1 = a;
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(x0, y0, 0.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(x0, y1, 0.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(x1, y1, 0.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(x1, y0, 0.0f);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public void drawText(String item, int x, int y, int color, boolean shadow) {
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        font.drawString(item, x, y, color, shadow);
    }
}
