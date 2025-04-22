package krazyminer001.playtime.datagen.lang;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class EnUsLangProvider extends FabricLanguageProvider {
    public EnUsLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, "en_us", registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add("disconnect.playtime.overtime", "You have exceeded your max daily playtime today of %s ticks, the next window where time is not tracked is %s");
        translationBuilder.add("playtime.playtime.timewindows.save_button", "Save");
        translationBuilder.add("playtime.playtime.timewindows.delete_button", "Delete");
        translationBuilder.add("playtime.playtime.timewindows.add_new_button", "Add New");
        translationBuilder.add("playtime.playtime.timewindows.save.invalid_time", "Invalid start or end time");
        translationBuilder.add("playtime.playtime.timewindows.invalid_time_period_change", "Invalid time period change, the time periods on the server may have changed since you opened the screen");
        translationBuilder.add("playtime.playtime.timewindows.start_time", "Start Time");
        translationBuilder.add("playtime.playtime.timewindows.end_time", "End Time");

        translationBuilder.add("commands.playtime.query.not_a_player", "You must be a player or specify a player target to run this command");
        translationBuilder.add("commands.playtime.query.user_playtime", "You have played: %s seconds today");
        translationBuilder.add("commands.playtime.query.player_playtime", "%s has played %s seconds today");
        translationBuilder.add("commands.playtime.set.success", "Successfully set playtime to %s ticks for player %s");
        translationBuilder.add("commands.playtime.timewindows.add.invalid_start_time", "Invalid startTime: %s");
        translationBuilder.add("commands.playtime.timewindows.add.invalid_end_time", "Invalid endTime: %s");
        translationBuilder.add("commands.playtime.timewindows.remove.invalid_start_time", "Invalid startTime: %s");
        translationBuilder.add("commands.playtime.timewindows.remove.invalid_end_time", "Invalid endTime: %s");
        translationBuilder.add("commands.playtime.timewindows.remove.non_existant_timewindow", "There is no period defined by startTime: %s and endTime: %s");

        translationBuilder.add("timeperiod.playtime.display", "startTime: %s, endTime: %s");
        translationBuilder.add("time.playtime.duration", "%s hours, %s minutes, %s seconds");

    }
}
