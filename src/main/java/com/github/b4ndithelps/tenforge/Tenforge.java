package com.github.b4ndithelps.tenforge;

import com.github.b4ndithelps.tenforge.capability.SkillEnergyCapability;
import com.github.b4ndithelps.tenforge.gui.SkillForgeMenu;
import com.github.b4ndithelps.tenforge.gui.SkillForgeScreen;
import com.github.b4ndithelps.tenforge.network.ModNetwork;
import com.github.b4ndithelps.tenforge.registry.ModMenuTypes;
import com.github.b4ndithelps.tenforge.registry.race.AllRaces;
import com.github.b4ndithelps.tenforge.registry.skill.AllSkills;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Tenforge - Skill Forge system for Tensura: Reincarnated
 * 
 * Features:
 * - Skill Forge: Break down and create skills using energy
 * - Extensible framework for lore-accurate Tensura skills
 * - Cross-mod compatibility with any ManasCore skill
 */
@Mod(Tenforge.MODID)
public class Tenforge {

    public static final String MODID = "tenforge";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Tenforge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // Register setup listeners
        modEventBus.addListener(this::commonSetup);

        // Register all components
        AllSkills.register(modEventBus);
        ModMenuTypes.register(modEventBus);

        // Register capability events
        MinecraftForge.EVENT_BUS.register(SkillEnergyCapability.class);
        modEventBus.addListener(SkillEnergyCapability::registerCapability);

        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Tenforge has been loaded! Skill Forge is ready.");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Register network packets
            ModNetwork.register();
            LOGGER.info("Tenforge network registered.");
        });
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Tenforge server starting...");
    }

    /**
     * Client-side setup and screen registration
     */
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("Tenforge client setup...");
            
            event.enqueueWork(() -> {
                // Register GUI screens
                MenuScreens.register(ModMenuTypes.SKILL_FORGE_MENU.get(), SkillForgeScreen::new);
                LOGGER.info("Tenforge screens registered.");
            });
            
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
