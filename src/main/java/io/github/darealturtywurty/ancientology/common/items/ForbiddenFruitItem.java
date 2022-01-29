package io.github.darealturtywurty.ancientology.common.items;

import static io.github.darealturtywurty.ancientology.core.util.Constants.RAND;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.darealturtywurty.ancientology.Ancientology;
import io.github.darealturtywurty.ancientology.core.init.ItemInit;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class ForbiddenFruitItem extends Item {
    public static final FoodProperties FOOD = new FoodProperties.Builder().alwaysEat().nutrition(10).saturationMod(0.1f)
            .build();

    private static final Component BAD_EFFECT = new TranslatableComponent(
            "msg." + Ancientology.MODID + ".fruit_give_bad_effect");
    private static final Component GOOD_EFFECT = new TranslatableComponent(
            "msg." + Ancientology.MODID + ".fruit_give_good_effect");
    private static final Component GIVE_ITEM = new TranslatableComponent(
            "msg." + Ancientology.MODID + ".fruit_give_item");
    private static final Component LIGHTNING = new TranslatableComponent(
            "msg." + Ancientology.MODID + ".fruit_lightning");
    private static final Component ARROW_RAIN = new TranslatableComponent(
            "msg." + Ancientology.MODID + ".fruit_arrow_rain");
    private static final Component HUNGER = new TranslatableComponent("msg." + Ancientology.MODID + ".fruit_hunger");
    private static final Component CHANCE = new TranslatableComponent("msg." + Ancientology.MODID + ".fruit_chance");
    private static final Component NETHER = new TranslatableComponent("msg." + Ancientology.MODID + ".fruit_nether");
    
    public ForbiddenFruitItem(Properties pProperties) {
        super(pProperties);
    }
    
    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        if (!pLevel.isClientSide) {
            useItem(pLevel, pLivingEntity);
        }
        
        return super.finishUsingItem(pStack, pLevel, pLivingEntity);
    }
    
    public void giveRandomBadEffect(LivingEntity entity) {
        List<MobEffect> effects = new ArrayList<>(ForgeRegistries.MOB_EFFECTS.getValues());
        effects = effects.stream().filter(mobEffect -> !mobEffect.isBeneficial()).toList();
        final MobEffect effect = effects.get(RAND.nextInt(effects.size()));
        final int duration = RAND.nextInt(1, 6) * 1200;
        final int amplifier = RAND.nextInt(5);
        final MobEffectInstance instance = new MobEffectInstance(effect, duration, amplifier);
        entity.addEffect(instance);
        entity.sendMessage(BAD_EFFECT, entity.getUUID());
    }
    
    public void giveRandomGoodEffect(LivingEntity entity) {
        List<MobEffect> effects = new ArrayList<>(ForgeRegistries.MOB_EFFECTS.getValues());
        effects = effects.stream().filter(MobEffect::isBeneficial).toList();
        final MobEffect effect = effects.get(RAND.nextInt(effects.size()));
        final int duration = RAND.nextInt(1, 6) * 1200;
        final int amplifier = RAND.nextInt(5);
        final MobEffectInstance instance = new MobEffectInstance(effect, duration, amplifier);
        entity.addEffect(instance);
        entity.sendMessage(GOOD_EFFECT, entity.getUUID());
    }
    
    public void giveRandomItem(LivingEntity entity) {
        List<Item> items = new ArrayList<>(ForgeRegistries.ITEMS.getValues());
        items = items.stream().filter(item -> item.getItemCategory() != null).toList();
        final List<Enchantment> enchantments = new ArrayList<>(ForgeRegistries.ENCHANTMENTS.getValues());
        
        final ItemStack stack = new ItemStack(items.get(RAND.nextInt(items.size())));
        
        final Enchantment enchant = enchantments.get(RAND.nextInt(enchantments.size()));
        final int eLevel = RAND.nextInt(enchant.getMaxLevel() + 1);
        if (eLevel != 0) {
            stack.enchant(enchant, eLevel);
        }
        
        if (entity instanceof final ServerPlayer player) {
            player.addItem(stack);
            entity.sendMessage(GIVE_ITEM, entity.getUUID());
        }
    }
    
    public void lightningStorm(LivingEntity entity, Level level) {
        entity.sendMessage(LIGHTNING, entity.getUUID());
        for (int j = 0; j < 10; j++) {
            final LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
            bolt.setPos(entity.getX(), entity.getY(), entity.getZ());
            level.addFreshEntity(bolt);
        }
    }
    
    public void rainOfArrows(LivingEntity entity, Level level) {
        entity.sendMessage(ARROW_RAIN, entity.getUUID());
        for (int j = 0; j < 30; j++) {
            final Arrow bolt = new Arrow(EntityType.ARROW, level);
            bolt.setPos(entity.getX(), entity.getY() + 1, entity.getZ());
            level.addFreshEntity(bolt);
        }
    }
    
    public void removeHunger(LivingEntity entity) {
        entity.sendMessage(HUNGER, entity.getUUID());
        if (entity instanceof final Player player) {
            player.getFoodData().setFoodLevel(0);
            player.getFoodData().setSaturation(0);
        }
    }
    
    public void secondChance(LivingEntity entity) {
        entity.sendMessage(CHANCE, entity.getUUID());
        if (entity instanceof final Player player) {
            player.addItem(new ItemStack(ItemInit.FORBIDDEN_FRUIT.get()));
        }
    }
    
    public void sendToNether(LivingEntity entity) {
        if (entity instanceof final ServerPlayer player) {
            final ServerLevel toLevel = Objects.requireNonNull(entity.level.getServer()).getLevel(Level.NETHER);
            player.teleportTo(toLevel, entity.blockPosition().getX(), Math.min(entity.blockPosition().getY(), 122),
                    entity.blockPosition().getZ(), 0, 0);
            entity.sendMessage(NETHER, entity.getUUID());
        }
    }
    
    private void useItem(Level level, LivingEntity entity) {
        final double d = RAND.nextDouble();
        final double f = RAND.nextDouble();
        if (d < 0.99) {
            if (f < 0.1) {
                sendToNether(entity);
            } else if (f < 0.2) {
                lightningStorm(entity, level);
            } else if (f < 0.25) {
                removeHunger(entity);
            } else if (f < 0.4) {
                giveRandomBadEffect(entity);
            } else if (f < 0.5) {
                rainOfArrows(entity, level);
            } else if (f < 0.52) {
                secondChance(entity);
            } else if (f < 0.6) {
                // TODO: Implement
            }
        } else if (f < 0.7) {
            giveRandomItem(entity);
        } else {
            giveRandomGoodEffect(entity);
        }
    }
}
