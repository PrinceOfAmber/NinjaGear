package com.InfinityRaider.ninjagear.registry;

import com.InfinityRaider.ninjagear.handler.ConfigurationHandler;
import com.InfinityRaider.ninjagear.potion.PotionNinjaAura;
import com.InfinityRaider.ninjagear.potion.PotionNinjaHidden;
import com.InfinityRaider.ninjagear.potion.PotionNinjaRevealed;
import com.InfinityRaider.ninjagear.reference.Reference;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;

public class PotionRegistry {
    private static final PotionRegistry INSTANCE = new PotionRegistry();

    public static PotionRegistry getInstance() {
        return INSTANCE;
    }

    private final RegistryNamespaced<ResourceLocation, Potion> potionRegistry;
    private int lastId = -1;

    public Potion potionNinjaAura;
    public Potion potionNinjaHidden;
    public Potion potionNinjaRevealed;

    private PotionRegistry() {
        this.potionRegistry = Potion.potionRegistry;
        getNextId();
    }

    public void init() {
        potionNinjaAura = this.registerPotion(new PotionNinjaAura());
        potionNinjaHidden = this.registerPotion(new PotionNinjaHidden());
        potionNinjaRevealed = this.registerPotion(new PotionNinjaRevealed());
    }

    public Potion registerPotion(Potion potion) {
        String name = potion.getName();
        int id = ConfigurationHandler.getInstance().getPotionEffectId(name, getNextId());
        this.potionRegistry.register(id, new ResourceLocation(Reference.MOD_ID, name), potion);
        return potion;
    }

    private int getNextId() {
        int id = lastId;
        boolean flag = false;
        while(!flag) {
            id = id + 1;
            Potion potion = potionRegistry.getObjectById(id);
            if(potion == null) {
                flag = true;
            }
        }
        lastId = id;
        return id;
    }



}
