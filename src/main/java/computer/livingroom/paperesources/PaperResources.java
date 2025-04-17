package computer.livingroom.paperesources;

import computer.livingroom.paperesources.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class PaperResources extends JavaPlugin {

    @Getter
    @Setter
    private ResourcePackRequest resourceRequest;
    @Getter
    private static PaperResources instance;
    private final File resourceFile = new File(getDataFolder(), "resources.txt");

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        //If anyone runs this plugin on 1.21> im paper will just throw a fit

        // Plugin startup logic
        getDataFolder().mkdir();
        try {
            resourceFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        saveDefaultConfig();

        if (getServer().getServerResourcePack() != null) {
            getLogger().warning("Attempting to override resource-pack in server.properties");
            try {
                File serverProperties = new File(Paths.get(this.getDataFolder().getParentFile().getCanonicalFile().getParentFile().toString() + File.separatorChar + "server.properties").toString());

                FileInputStream in = new FileInputStream(serverProperties);
                Properties properties = new Properties();
                properties.load(in);
                in.close();

                FileOutputStream out = new FileOutputStream(serverProperties);
                properties.setProperty("resource-pack", "");
                properties.store(out, null);
                out.close();
                getLogger().warning("Done! Please restart the server to ensure changes take affect!");
            } catch (IOException e) {
                getLogger().severe("Could not access server properties file!");
            }
        }

        createResourceRequest();

        getServer().getPluginManager().registerEvents(new PaperResourcesListener(), this);
        new PaperResourcesCommand().registerCommand(getCommand("paperresources"));

    }


    public static class Config {
        public static String getPrompt() {
            return PaperResources.getInstance().getConfig().getString("prompt", "");
        }

        public static boolean isRequired() {
            return PaperResources.getInstance().getConfig().getBoolean("required", false);
        }
    }

    public void createResourceRequest() {
        try {
            List<ResourcePackInfo> resourcePacks = new ArrayList<>();
            List<String> list = Files.readAllLines(resourceFile.toPath());
            if (list.isEmpty())
                resourceRequest = null;

            //packs are like a stack and whatever is loaded last is on top in terms of priority, to make it simpler just reverse list so item #1 is always put on the stack last.
            for (String s : list.reversed()) {
                if (s.isBlank())
                    continue;

                this.getLogger().info("Loading pack: " + s + "...");
                ResourcePackInfo info;
                try {
                    info = ResourcePackInfo.resourcePackInfo().uri(URI.create(s)).computeHashAndBuild().get();
                } catch (Exception e) {
                    getLogger().severe("Failed to load pack: " + s);
                    getLogger().severe("Skipping...");
                    continue;
                }
                this.getLogger().info("Loaded!");
                resourcePacks.add(info);
            }
            ResourcePackRequest.Builder builder = ResourcePackRequest.resourcePackRequest()
                    .packs(resourcePacks)
                    .replace(true)
                    .required(Config.isRequired());
            String prompt = Config.getPrompt();
            if (!prompt.isBlank())
                builder.prompt(Component.text(prompt));

            resourceRequest = builder.build();

        } catch (IOException e) {
            resourceRequest = null;
        }
    }
}
