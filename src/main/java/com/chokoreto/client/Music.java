package com.chokoreto.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/** A tiny music player that plays Minecraft's own music discs (no files needed). */
public final class Music {
    private Music() {}

    public static final String[] NAMES = {
            "13", "Cat", "Blocks", "Chirp", "Far", "Mall", "Mellohi", "Stal", "Strad", "Ward",
            "11", "Wait", "Pigstep", "Otherside", "5", "Relic", "Creator", "Creator (Music Box)", "Precipice"
    };
    public static final String[] IDS = {
            "13", "cat", "blocks", "chirp", "far", "mall", "mellohi", "stal", "strad", "ward",
            "11", "wait", "pigstep", "otherside", "5", "relic", "creator", "creator_music_box", "precipice"
    };

    private static SimpleSoundInstance playing;
    private static String nowPlaying = null;

    public static String nowPlaying() {
        return nowPlaying;
    }

    public static void play(int index) {
        Minecraft mc = Minecraft.getInstance();
        stop();
        Identifier id = Identifier.tryParse("minecraft:music_disc." + IDS[index]);
        if (id == null) return;
        SoundEvent event = BuiltInRegistries.SOUND_EVENT.getValue(id);
        if (event == null) {
            AuctionManager.info("That track is not in this version of Minecraft.");
            return;
        }
        float volume = Config.get().musicVolume / 100f;
        playing = SimpleSoundInstance.forUI(event, 1.0f, volume);
        mc.getSoundManager().play(playing);
        nowPlaying = NAMES[index];
    }

    public static void stop() {
        if (playing != null) {
            Minecraft.getInstance().getSoundManager().stop(playing);
            playing = null;
        }
        nowPlaying = null;
    }
}
