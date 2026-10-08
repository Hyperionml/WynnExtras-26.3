package julianh06.wynnextras.core;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class WynnExtrasSounds {
    public static final Identifier yesSound = Identifier.fromNamespaceAndPath("wynnextras", "yes");
    public static final Identifier noSound = Identifier.fromNamespaceAndPath("wynnextras", "no");
    public static final Identifier no2Sound = Identifier.fromNamespaceAndPath("wynnextras", "no2");
    public static final Identifier nothingSound = Identifier.fromNamespaceAndPath("wynnextras", "nothing");
    public static final Identifier nothing2Sound = Identifier.fromNamespaceAndPath("wynnextras", "nothing2");
    public static final Identifier idtsSound = Identifier.fromNamespaceAndPath("wynnextras", "idts");
    public static final Identifier askagainSound = Identifier.fromNamespaceAndPath("wynnextras", "askagain");
    public static final Identifier neitherSound = Identifier.fromNamespaceAndPath("wynnextras", "neither");

    public static final SoundEvent yes = SoundEvent.createVariableRangeEvent(yesSound);
    public static final SoundEvent no = SoundEvent.createVariableRangeEvent(noSound);
    public static final SoundEvent no2 = SoundEvent.createVariableRangeEvent(no2Sound);
    public static final SoundEvent nothing = SoundEvent.createVariableRangeEvent(nothingSound);
    public static final SoundEvent nothing2 = SoundEvent.createVariableRangeEvent(nothing2Sound);
    public static final SoundEvent idts = SoundEvent.createVariableRangeEvent(idtsSound);
    public static final SoundEvent askagain = SoundEvent.createVariableRangeEvent(askagainSound);
    public static final SoundEvent neither = SoundEvent.createVariableRangeEvent(neitherSound);

    public static void register() {
        Registry.register(BuiltInRegistries.SOUND_EVENT, yesSound, yes);
        Registry.register(BuiltInRegistries.SOUND_EVENT, noSound, no);
        Registry.register(BuiltInRegistries.SOUND_EVENT, no2Sound, no2);
        Registry.register(BuiltInRegistries.SOUND_EVENT, nothingSound, nothing);
        Registry.register(BuiltInRegistries.SOUND_EVENT, nothing2Sound, nothing2);
        Registry.register(BuiltInRegistries.SOUND_EVENT, idtsSound, idts);
        Registry.register(BuiltInRegistries.SOUND_EVENT, askagainSound, askagain);
        Registry.register(BuiltInRegistries.SOUND_EVENT, neitherSound, neither);
    }
}
