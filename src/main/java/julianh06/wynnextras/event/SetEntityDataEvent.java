package julianh06.wynnextras.event;

import julianh06.wynnextras.event.api.WEEvent;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SetEntityDataEvent extends WEEvent {
    private final int id;
    private final List<SynchedEntityData.DataValue<?>> packedItems;

    public SetEntityDataEvent(ClientboundSetEntityDataPacket packet) {
        this.id = packet.id();
        this.packedItems = new ArrayList<>(packet.packedItems());
    }

    public int getId() {
        return id;
    }

    public List<SynchedEntityData.DataValue<?>> getPackedItems() {
        return Collections.unmodifiableList(packedItems);
    }

    public void addPackedItem(SynchedEntityData.DataValue<?> packedItem) {
        packedItems.add(packedItem);
    }

    public void removePackedItem(SynchedEntityData.DataValue<?> packedItem) {
        packedItems.remove(packedItem);
    }
}
