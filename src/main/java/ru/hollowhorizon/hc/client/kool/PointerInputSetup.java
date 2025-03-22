package ru.hollowhorizon.hc.client.kool;

import de.fabmax.kool.input.*;
import org.lwjgl.glfw.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static ru.hollowhorizon.hc.client.kool.HelperKt.KEY_CODE_MAP;

public class PointerInputSetup {
    private static GLFWMouseButtonCallback mouseButtonOld = null;
    private static GLFWCursorPosCallback cursorPosOld = null;
    private static GLFWCursorEnterCallback cursorEnterOld = null;
    private static GLFWScrollCallback scrollOld = null;
    private static GLFWKeyCallback keyOld = null;
    private static GLFWCharCallback charOld = null;
    private static final HashMap<Integer, Integer> localCharKeyCodes = new HashMap<>();

    private void deriveLocalKeyCodes() {
        List<Integer> printableKeys = new ArrayList<>();

        // Добавляем цифровые клавиши
        for (int c = GLFW.GLFW_KEY_0; c <= GLFW.GLFW_KEY_9; c++) {
            printableKeys.add(c);
        }
        // Добавляем буквенные клавиши
        for (int c = GLFW.GLFW_KEY_A; c <= GLFW.GLFW_KEY_Z; c++) {
            printableKeys.add(c);
        }
        // Добавляем клавиши на цифровой клавиатуре
        for (int c = GLFW.GLFW_KEY_KP_0; c <= GLFW.GLFW_KEY_KP_9; c++) {
            printableKeys.add(c);
        }

        // Добавляем другие клавиши
        printableKeys.add(GLFW.GLFW_KEY_APOSTROPHE);
        printableKeys.add(GLFW.GLFW_KEY_COMMA);
        printableKeys.add(GLFW.GLFW_KEY_MINUS);
        printableKeys.add(GLFW.GLFW_KEY_PERIOD);
        printableKeys.add(GLFW.GLFW_KEY_SLASH);
        printableKeys.add(GLFW.GLFW_KEY_SEMICOLON);
        printableKeys.add(GLFW.GLFW_KEY_EQUAL);
        printableKeys.add(GLFW.GLFW_KEY_LEFT_BRACKET);
        printableKeys.add(GLFW.GLFW_KEY_RIGHT_BRACKET);
        printableKeys.add(GLFW.GLFW_KEY_BACKSLASH);
        printableKeys.add(GLFW.GLFW_KEY_KP_DECIMAL);
        printableKeys.add(GLFW.GLFW_KEY_KP_DIVIDE);
        printableKeys.add(GLFW.GLFW_KEY_KP_MULTIPLY);
        printableKeys.add(GLFW.GLFW_KEY_KP_SUBTRACT);
        printableKeys.add(GLFW.GLFW_KEY_KP_ADD);
        printableKeys.add(GLFW.GLFW_KEY_KP_EQUAL);

        // Обработка клавиш
        for (int c : printableKeys) {
            String localName = GLFW.glfwGetKeyName(c, 0);
            if (localName != null && !localName.isBlank()) {
                char localChar = Character.toUpperCase(localName.charAt(0));
                localCharKeyCodes.put(c, (int) localChar);
            }
        }
    }

    public static void setup(long windowHandle) {
        mouseButtonOld = GLFW.glfwSetMouseButtonCallback(windowHandle, (handle, btn, act, mods) -> {
            PointerInput.INSTANCE.handleMouseButtonEvent$kool_core(btn, act == GLFW.GLFW_PRESS);
            if (mouseButtonOld != null) mouseButtonOld.invoke(handle, btn, act, mods);
        });
        cursorPosOld = GLFW.glfwSetCursorPosCallback(windowHandle, (handle, x, y) -> {
            PointerInput.INSTANCE.handleMouseMove$kool_core((float) x, (float) y);
            if (cursorPosOld != null) cursorPosOld.invoke(handle, x, y);
        });
        cursorEnterOld = GLFW.glfwSetCursorEnterCallback(windowHandle, (handle, entered) -> {
            if (!entered) {
                PointerInput.INSTANCE.handleMouseExit$kool_core();
            }
            if (cursorEnterOld != null) cursorEnterOld.invoke(handle, entered);
        });
        scrollOld = GLFW.glfwSetScrollCallback(windowHandle, (handle, xOff, yOff) -> {
            PointerInput.INSTANCE.handleMouseScroll$kool_core((float) xOff, (float) yOff);
            if (scrollOld != null) scrollOld.invoke(handle, xOff, yOff);
        });

        // install keyboard callbacks
        keyOld = GLFW.glfwSetKeyCallback(windowHandle, (handle, key, scancode, action, mods) -> {
            int event = switch (action) {
                case GLFW.GLFW_PRESS -> KeyboardInput.KEY_EV_DOWN;
                case GLFW.GLFW_REPEAT -> KeyboardInput.KEY_EV_DOWN | KeyboardInput.KEY_EV_REPEATED;
                case GLFW.GLFW_RELEASE -> KeyboardInput.KEY_EV_UP;
                default -> -1;
            };

            if (event != -1) {
                KeyCode keyCode = KEY_CODE_MAP.getOrDefault(key, new UniversalKeyCode(key, null));
                LocalKeyCode localKeyCode = new LocalKeyCode(localCharKeyCodes.getOrDefault(keyCode.getCode(), keyCode.getCode()), null);
                int keyMod = getKeyMod(key, mods, event);

                KeyboardInput.INSTANCE.handleKeyEvent(new KeyEvent(keyCode, localKeyCode, event, keyMod, Character.MIN_VALUE));
            }

            if (keyOld != null) keyOld.invoke(handle, key, scancode, action, mods);
        });

        charOld = GLFW.glfwSetCharCallback(windowHandle, (handle, codepoint) -> {
            KeyboardInput.INSTANCE.handleCharTyped((char) codepoint);
            if (charOld != null) charOld.invoke(handle, codepoint);
        });
    }

    private static int getKeyMod(int key, int mods, int event) {
        int keyMod = 0;

        if ((mods & GLFW.GLFW_MOD_ALT) != 0) keyMod |= KeyboardInput.KEY_MOD_ALT;
        if ((mods & GLFW.GLFW_MOD_CONTROL) != 0) keyMod |= KeyboardInput.KEY_MOD_CTRL;
        if ((mods & GLFW.GLFW_MOD_SHIFT) != 0) keyMod |= KeyboardInput.KEY_MOD_SHIFT;
        if ((mods & GLFW.GLFW_MOD_SUPER) != 0) keyMod |= KeyboardInput.KEY_MOD_SUPER;

        switch (key) {
            case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT ->
                    keyMod = updateDownMask(keyMod, KeyboardInput.KEY_MOD_SHIFT, event);
            case GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL ->
                    keyMod = updateDownMask(keyMod, KeyboardInput.KEY_MOD_CTRL, event);
            case GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT ->
                    keyMod = updateDownMask(keyMod, KeyboardInput.KEY_MOD_ALT, event);
            case GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER ->
                    keyMod = updateDownMask(keyMod, KeyboardInput.KEY_MOD_SUPER, event);
        }
        return keyMod;
    }

    private static int updateDownMask(int mask, int bit, int event) {
        if ((event & KeyboardInput.KEY_EV_DOWN) != 0) {
            return mask | bit;
        } else {
            return mask & ~bit;
        }
    }

}
