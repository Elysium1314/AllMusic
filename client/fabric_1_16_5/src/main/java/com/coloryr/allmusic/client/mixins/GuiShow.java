package com.coloryr.allmusic.client.mixins;

import com.coloryr.allmusic.client.AllMusicClient;
import com.coloryr.allmusic.client.core.AllMusicCore;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiShow {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getArmor(I)Lnet/minecraft/world/item/ItemStack;"))
    public void Gui(PoseStack poseStack, float partialTick, CallbackInfo info) {
        AllMusicClient.update(poseStack);
        AllMusicCore.hudUpdate();
    }
}
