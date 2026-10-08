package julianh06.wynnextras.compat.wynntils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;

public final class WynntilsEventAdapter {
    private record ContainerClickBinding(Constructor<?> constructor, Method post, Method isCanceled) {}

    private static final WynntilsCapability<ContainerClickBinding> CONTAINER_CLICK = new WynntilsCapability<>(
            "container-click-event",
            () -> {
                Class<?> eventClass = WynntilsCompat.requireClass("com.wynntils.mc.event.ContainerClickEvent");
                Constructor<?> constructor = eventClass.getConstructor(
                        AbstractContainerMenu.class, int.class, ContainerInput.class, int.class);
                Class<?> mixinHelper = WynntilsCompat.requireClass("com.wynntils.core.events.MixinHelper");
                Method post = null;
                for (Method method : mixinHelper.getMethods()) {
                    if (method.getName().equals("post") && method.getParameterCount() == 1
                            && method.getParameterTypes()[0].isAssignableFrom(eventClass)) {
                        post = method;
                        break;
                    }
                }
                if (post == null) throw new NoSuchMethodException("MixinHelper.post(event)");
                Method isCanceled = eventClass.getMethod("isCanceled");
                return new ContainerClickBinding(constructor, post, isCanceled);
            }
    );

    private WynntilsEventAdapter() {}

    public static boolean postContainerClick(AbstractContainerMenu handler, int slot, ContainerInput action, int button) {
        if (handler == null) return false;
        return CONTAINER_CLICK.invoke(binding -> {
            Object event = binding.constructor.newInstance(handler, slot, action, button);
            binding.post.invoke(null, event);
            return (boolean) binding.isCanceled.invoke(event);
        }).orElse(false);
    }
}
