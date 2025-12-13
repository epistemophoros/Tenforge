package com.github.b4ndithelps.tenaddex;

import com.github.b4ndithelps.tenaddex.capability.SkillEnergyCapability;
import com.github.b4ndithelps.tenaddex.gui.SkillForgeMenu;
import com.github.b4ndithelps.tenaddex.gui.SkillForgeScreen;
import com.github.b4ndithelps.tenaddex.network.ModNetwork;
import com.github.b4ndithelps.tenaddex.registry.ModMenuTypes;
import com.github.b4ndithelps.tenaddex.registry.race.AllRaces;
import com.github.b4ndithelps.tenaddex.registry.skill.AllSkills;
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
 * TensuraWEL - Tensura Addon for Skill Forge and more
 * 
 * Features:
 * - Skill Forge: Break down and create skills using energy
 * - Extensible framework for lore-accurate Tensura skills
 */
@Mod(TensuraAddonExample.MODID)
public class TensuraAddonExample {

    public static final String MODID = "tenaddex";
    private static final Logger LOGGER = LogUtils.getLogger();

    public TensuraAddonExample() {
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
        LOGGER.info("TensuraWEL has been loaded! Skill Forge is ready.");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Register network packets
            ModNetwork.register();
            LOGGER.info("TensuraWEL network registered.");
        });
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("TensuraWEL server starting...");
    }

    /**
     * Client-side setup and screen registration
     */
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("TensuraWEL client setup...");
            
            event.enqueueWork(() -> {
                // Register GUI screens
                MenuScreens.register(ModMenuTypes.SKILL_FORGE_MENU.get(), SkillForgeScreen::new);
                LOGGER.info("TensuraWEL screens registered.");
            });
            
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
