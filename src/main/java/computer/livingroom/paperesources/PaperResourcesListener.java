package computer.livingroom.paperesources;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PaperResourcesListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPLayerJoin(PlayerJoinEvent event) {
        PaperResources.getInstance().getResourcesManager().sendRequestToPlayer(event.getPlayer());
    }
}
