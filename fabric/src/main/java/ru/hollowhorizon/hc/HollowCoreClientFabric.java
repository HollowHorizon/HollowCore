package ru.hollowhorizon.hc;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import ru.hollowhorizon.hc.client.HollowCoreClient;
import ru.hollowhorizon.hc.client.screens.ImGuiScreen;
import ru.hollowhorizon.hc.common.registry.ModShaders;

import javax.swing.text.JTextComponent;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class HollowCoreClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CoreShaderRegistrationCallback.EVENT.register(ctx ->
                ctx.register(new ResourceLocation(MODID, "gltf_entity"), DefaultVertexFormat.NEW_ENTITY, shader ->
                        ModShaders.GLTF_ENTITY = shader
                )
        );

        var bind = new KeyMapping(
                "key.examplemod.spook", // The translation key of the keybinding's name
                InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_V, // The keycode of the key
                "category.examplemod.test" // The translation key of the keybinding's category.
        );
        KeyBindingHelper.registerKeyBinding(bind);
        ClientTickEvents.END_CLIENT_TICK.register(c->{
            if(bind.isDown()) Minecraft.getInstance().setScreen(new ImGuiScreen());
        });

        var init = HollowCoreClient.INSTANCE; // Loading Main Class
    }
}
