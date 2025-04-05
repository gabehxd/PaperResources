package computer.livingroom.paperesources.utils.brigadier;

import com.google.common.collect.ObjectArrays;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Creates autocomplete suggestions and onCommand handling automatically.
 */
public class BrigadierExecutor implements TabExecutor {
    protected final CommandDispatcher<CommandSender> commandDispatcher;

    /**
     * Returning 1 from an executor in the LiteralArgumentBuilder will act as true and 0 and below will act as false.
     *
     * @param dispatcherConsumer The consumer to register commands with.
     */
    public BrigadierExecutor(@NonNull Consumer<CommandDispatcher<CommandSender>> dispatcherConsumer) {
        this.commandDispatcher = new CommandDispatcher<>();
        dispatcherConsumer.accept(commandDispatcher);
    }

    public void registerCommand(PluginCommand command) {
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    @Override
    public final boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias, @NonNull String[] args) {
        try {
            int result = this.commandDispatcher.execute(getCommandString(alias, args), sender);
            if (result <= 0) {
                sendUsageMessage(sender, this.commandDispatcher);
                return true;
            }
        } catch (CommandSyntaxException e) {
            if (e.getMessage() != null)
                sender.sendMessage(Component.text(e.getMessage(), NamedTextColor.RED));

            sendUsageMessage(sender, this.commandDispatcher);
            return true;
        }
        return true;
    }

    @Override
    public final @NonNull List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias, @NonNull String[] args) {
        String commandString = getCommandString(alias, args);
        Suggestions suggestions = this.commandDispatcher.getCompletionSuggestions(this.commandDispatcher.parse(commandString, sender)).join();
        return suggestions.getList().stream().map(Suggestion::getText).collect(Collectors.toList());
    }

    private static void sendUsageMessage(@NonNull CommandSender sender, @NonNull CommandDispatcher<CommandSender> commandDispatcher) {
        sender.sendMessage(Component.text("Usages:", NamedTextColor.RED));
        for (String s : commandDispatcher.getAllUsage(commandDispatcher.getRoot(), sender, true))

            sender.sendMessage(
                    Component.text().append(Component.text("/", NamedTextColor.RED).append(Component.text(s, NamedTextColor.RED)))
                            .build());
    }

    public final String getCommandString(String alias, String[] args) {
        return String.join(" ", ObjectArrays.concat(alias, args));
    }
}
