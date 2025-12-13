package com.github.b4ndithelps.tenforge.registry;

import com.github.b4ndithelps.tenforge.Tenforge;
import com.github.b4ndithelps.tenforge.gui.SkillForgeMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry for all menu types (GUIs) in the addon.
 */
public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS = 
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Tenforge.MODID);

    public static final RegistryObject<MenuType<SkillForgeMenu>> SKILL_FORGE_MENU =
            MENUS.register("skill_forge", () -> IForgeMenuType.create(SkillForgeMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}

