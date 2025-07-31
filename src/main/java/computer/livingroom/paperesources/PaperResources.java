package computer.livingroom.paperesources;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

public final class PaperResources extends JavaPlugin {
    @Getter
    private static PaperResources instance;
    @Getter
    private PaperResourcesManager resourcesManager;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance.getDataFolder().mkdir();
        instance.saveDefaultConfig();

        if (getServer().getServerResourcePack() != null) {
            getLogger().warning("Attempting to override resource-pack in server.properties");
            try {
                File serverProperties = new File(Paths.get(this.getDataFolder().getParentFile().getCanonicalFile().getParentFile().toString() + File.separatorChar + "server.properties").toString());

                FileInputStream in = new FileInputStream(serverProperties);
                Properties properties = new Properties();
                properties.load(in);

                FileOutputStream out = new FileOutputStream(serverProperties);
                properties.setProperty("resource-pack", "");
                properties.store(out, null);
                in.close();
                out.close();
                getLogger().warning("Done! Please restart the server to ensure changes take affect!");
            } catch (IOException e) {
                getLogger().severe("Could not access server properties file!");
            }
        }
        resourcesManager = new PaperResourcesManager(this);
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
}
