package com.storycraft.pvputil.module.hitsound;

import com.storycraft.pvputil.PvpUtil;
import com.storycraft.pvputil.config.json.JsonConfigEntry;
import com.storycraft.pvputil.module.IModule;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class PlayerHitSound implements IModule {

    public static final String OPTION_CATEGORY = "hitsound";

    private PvpUtil mod;

    private boolean soundEnabled;

    private ResourceLocation soundHitNormalLoc;
    private ResourceLocation soundHitClapLoc;
    private ResourceLocation soundHitFinishLoc;

    private boolean sprint;
    private long sprintStart;

    @Override
    public void preInitialize() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void initialize(PvpUtil mod) {
        this.mod = mod;

        this.soundEnabled = isModEnabled();

        this.soundHitNormalLoc = new ResourceLocation(PvpUtil.getModMetadata().modId, "hitsound.normal");
        this.soundHitClapLoc = new ResourceLocation(PvpUtil.getModMetadata().modId, "hitsound.clap");
        this.soundHitFinishLoc = new ResourceLocation(PvpUtil.getModMetadata().modId, "hitsound.finish");

        this.sprintStart = 0;
        this.sprint = false;
    }

    @SubscribeEvent
    public void capturePlayerSprint(PlayerTickEvent e) {
        if (e.player.isUser()) {
            if (sprint != e.player.isSprinting()) {
                sprint = e.player.isSprinting();

                if (sprint) {
                    sprintStart = System.currentTimeMillis();
                }
            }
        }
    }

    @SubscribeEvent
    public void onLeftInteract(AttackEntityEvent e){
        if (e.entityPlayer == null || !e.entityPlayer.isUser() || !soundEnabled){
            return;
        }

        Entity target = e.target;
        EntityPlayer attacker = e.entityPlayer;

        boolean crit = attacker.fallDistance > 0.0F && !attacker.onGround && !attacker.isOnLadder() && !attacker.isInWater() && !attacker.isPotionActive(Potion.blindness) && !attacker.isRiding() && !attacker.isSprinting();

        World world = attacker.getEntityWorld();

        world.playSound(target.posX, target.posY, target.posZ, soundHitNormalLoc.toString(), 1f, 1f, false);

        if (attacker.isSprinting() && (System.currentTimeMillis() - sprintStart) <= 300) { //W tap
            world.playSound(target.posX, target.posY, target.posZ, soundHitClapLoc.toString(), 1f, 1f, false);
        }
    
        if (crit) { //Crit
            world.playSound(target.posX, target.posY, target.posZ, soundHitFinishLoc.toString(), 1f, 1f, false);
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.PostConfigChangedEvent e) {
        this.soundEnabled = isModEnabled();
    }

    public boolean isModEnabled() {
        if (!getModuleConfigEntry().contains("sound_when_click_entity"))
            getModuleConfigEntry().set("sound_when_click_entity", true);

        return getModuleConfigEntry().get("sound_when_click_entity").getAsBoolean();
    }

    public JsonConfigEntry getModuleConfigEntry(){
        if (!mod.getDefaultConfig().contains(OPTION_CATEGORY)) {
            mod.getDefaultConfig().set(OPTION_CATEGORY, new JsonConfigEntry());
        }

        return mod.getDefaultConfig().getObject(OPTION_CATEGORY);
    }
}
