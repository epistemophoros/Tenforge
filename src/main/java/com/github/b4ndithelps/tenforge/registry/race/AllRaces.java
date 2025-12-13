package com.github.b4ndithelps.tenforge.registry.race;

import com.github.b4ndithelps.tenforge.Tenforge;
import com.github.manasmods.tensura.race.Race;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

/**
 * Race registry for Tenforge.
 * Register custom races here if needed.
 */
public class AllRaces {

    public static final DeferredRegister<Race> RACES = 
            DeferredRegister.create(Race.getRegistryKey(), Tenforge.MODID);

    public static void register(IEventBus modEventBus) {
        RACES.register(modEventBus);
    }

    // Add custom races here as needed
}
