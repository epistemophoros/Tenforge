package com.github.b4ndithelps.tenaddex.registry.race;

import com.github.b4ndithelps.tenaddex.TensuraAddonExample;
import com.github.b4ndithelps.tenaddex.race.ExampleRace;
import com.github.manasmods.tensura.registry.race.TensuraRaces;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = TensuraAddonExample.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AllRaces {

    // Define the Race that you want to add here
    public static final ResourceLocation EXAMPLE_RACE = new ResourceLocation(TensuraAddonExample.MODID, "example_race");

    /**
     * Make sure that you register the race, otherwise it will not show up correctly in the selection menu
     * @param event
     */
    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(((IForgeRegistry) TensuraRaces.RACE_REGISTRY.get()).getRegistryKey(), helper -> {
            helper.register("example_race", new ExampleRace());
        });
    }

}
