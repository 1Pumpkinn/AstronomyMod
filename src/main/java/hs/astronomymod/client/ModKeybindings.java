package hs.astronomymod.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ModKeybindings {
    public static KeyBinding ACTIVATE_ASTRONOMY_ABILITY;
    public static KeyBinding SELECT_ASTRONOMY_SLOT;

    // Correct category using Identifier.of
    public static final Category ASTRONOMY_CATEGORY = new Category(
            Identifier.of("astronomymod:astronomy")
    );

    public static void registerKeybindings() {
        ACTIVATE_ASTRONOMY_ABILITY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.astronomymod.ability_1",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                ASTRONOMY_CATEGORY
        ));

        SELECT_ASTRONOMY_SLOT = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.astronomymod.ability_2",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                ASTRONOMY_CATEGORY
        ));
    }
}