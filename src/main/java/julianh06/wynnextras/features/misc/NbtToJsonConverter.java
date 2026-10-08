package julianh06.wynnextras.features.misc;

import com.google.gson.JsonElement;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

public class NbtToJsonConverter {
    public static JsonElement convert(Tag nbt) {
        Dynamic<Tag> dynamic = new Dynamic<>(NbtOps.INSTANCE, nbt);
        Dynamic<JsonElement> jsonDynamic = dynamic.convert(JsonOps.INSTANCE);
        return jsonDynamic.getValue();
    }
}
