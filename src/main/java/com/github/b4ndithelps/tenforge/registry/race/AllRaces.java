package com.github.b4ndithelps.tenforge.registry.race;

import com.github.b4ndithelps.tenforge.Tenforge;
import com.github.b4ndithelps.tenforge.race.ExampleRace;
import com.github.manasmods.tensura.registry.race.TensuraRaces;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = Tenforge.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AllRaces {

    // Define the Race that you want to add here
    public static final ResourceLocation EXAMPLE_RACE = new ResourceLocation(Tenforge.MODID, "example_race");

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
