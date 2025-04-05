package computer.livingroom.paperesources;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PaperResourcesListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPLayerJoin(PlayerJoinEvent event) {
        if (PaperResources.getInstance().getResourceRequest() != null)
            event.getPlayer().sendResourcePacks(PaperResources.getInstance().getResourceRequest());
    }
}
