package ru.hollowhorizon.hc.client.utils;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;

import static ru.hollowhorizon.hc.HollowCore.MODID;


public class HollowKeyHandler {

    //private static final KeyBinding OPEN_EVENT_LIST;


    private static final String KEY_CATEGORY_MOD = String.format("key.categories.mod.%s", MODID);


    //OPEN_EVENT_LIST = new KeyBinding(keyBindName("event_list"), 342, KEY_CATEGORY_MOD);


    @OnlyIn(Dist.CLIENT)
    public HollowKeyHandler() {
        registerKeys();
    }

    private static String keyBindName(String name) {
        return String.format("key.%s.%s", MODID, name);
    }

    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        //if (OPEN_EVENT_LIST.isActiveAndMatches(InputMappings.getKey(event.getKey(), event.getScanCode())) && Minecraft.getInstance().screen == null) {
            //Minecraft.getInstance().setScreen(new EventListScreen(new ArrayList<>()));
        //}
    }

    private void registerKeys() {
        //ClientRegistry.registerKeyBinding(OPEN_EVENT_LIST);
    }
}