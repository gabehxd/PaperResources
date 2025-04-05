package computer.livingroom.paperesources;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import computer.livingroom.paperesources.utils.brigadier.BrigadierExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class PaperResourcesCommand extends BrigadierExecutor {
    public PaperResourcesCommand() {
        super(dispatcher -> dispatcher.register(LiteralArgumentBuilder.<CommandSender>literal("paperresources")
                .then(LiteralArgumentBuilder.<CommandSender>literal("reload")
                        .executes(ctx -> {
                            ctx.getSource().sendMessage(Component.text("Reloading resources!", NamedTextColor.GOLD));

                            PaperResources.getInstance().createResourceRequest();
                            if (PaperResources.getInstance().getResourceRequest() != null)
                                Bukkit.getServer().sendResourcePacks(PaperResources.getInstance().getResourceRequest());

                            ctx.getSource().sendMessage(Component.text("Reloaded!", NamedTextColor.GOLD));
                            return 1;
                        })
                )
        ));
    }
}
