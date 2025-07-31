package computer.livingroom.paperesources;

import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PaperResourcesManager {
    private ResourcePackRequest resourceRequest;
    private final File resourceFile;
    private final Logger logger;

    public PaperResourcesManager(PaperResources instance) {
        resourceFile = new File(PaperResources.getInstance().getDataFolder(), "resources.txt");
        logger = instance.getLogger();
        try {
            resourceFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        createRequest();
    }

    /**
     * This will cause the plugin to load any changes in the resource file (if any) and save your pack to whatever index you indicate.
     *
     * @param url   String of url that points towards the pack file
     * @param index Priority of the pack or Index within the resource file (index 0 will always load first)
     */
    public void addResourcePack(String url, int index, boolean rebuild, boolean resendResources) {
        List<String> urls;
        try {
            urls = getURLStrings();
        } catch (IOException e) {
            logger.severe("Unable to open resource file");
            return;
        }
        urls.add(index, url);
        createRequest(urls);
        saveUrls(urls);

        if (rebuild) {
            createRequest(urls);
            if (resendResources && isRequestValid()) {
                Bukkit.getServer().sendResourcePacks(resourceRequest);
            }
        }
    }

    public boolean removeResourcePack(String url, boolean rebuild, boolean resendResources) {
        List<String> urls;
        try {
            urls = getURLStrings();
        } catch (IOException e) {

            logger.severe("Unable to open resource file");
            return false;
        }
        boolean rc = urls.remove(url);

        if (rebuild) {
            createRequest(urls);
            if (resendResources && isRequestValid()) {
                Bukkit.getServer().sendResourcePacks(resourceRequest);
            }
        }

        saveUrls(urls);

        return rc;
    }

    /**
     * @return Returns the urls from the resource file.
     */
    public List<String> getURLStrings() throws IOException {
        return Files.readAllLines(resourceFile.toPath());
    }

    private void saveUrls(List<String> urls)
    {
        try {
            Files.write(resourceFile.toPath(), urls, Charset.defaultCharset());
        } catch (IOException e) {
            logger.severe("Unable to save resource file");
        }
    }

    /**
     * @return Whether the current resource request is valid and is not empty.
     */
    public boolean isRequestValid() {
        return resourceRequest != null && !resourceRequest.packs().isEmpty();
    }

    /**
     * Sends specified player the resource packs generated. Request validation is already done.
     *
     * @param player Player to send resource packs to.
     */
    public void sendRequestToPlayer(Player player) {
        if (isRequestValid())
            player.sendResourcePacks(resourceRequest);
    }

    /**
     * Reloads the resource packs from the resource file.
     *
     * @param sendNewResources Whether to resend the resource packs to all players or wait (usually until they relog)
     */
    public void reloadResources(boolean sendNewResources) {
        createRequest();
        if (sendNewResources && isRequestValid()) {
            Bukkit.getServer().sendResourcePacks(resourceRequest);
        }
    }

    private void createRequest() {
        try {
            createRequest(getURLStrings());
        } catch (IOException e) {
            logger.severe("Unable to open resource file");
            resourceRequest = null;
        }
    }

    private void createRequest(List<String> packs) {
        List<ResourcePackInfo> resourcePacks = new ArrayList<>();
        if (packs.isEmpty()) {
            resourceRequest = null;
            return;
        }

        //packs are like a stack and whatever is loaded last is on top in terms of priority, to make it simpler just reverse list so item #1 is always put on the stack last.
        for (String s : packs.reversed()) {
            if (s.isBlank())
                continue;

            logger.info("Loading pack: " + s + "...");
            ResourcePackInfo info;
            try {
                info = ResourcePackInfo.resourcePackInfo().uri(URI.create(s)).computeHashAndBuild().get();
            } catch (Exception e) {
                logger.severe("Failed to load pack: " + s);
                logger.severe("Skipping...");
                continue;
            }
            logger.info("Loaded!");
            resourcePacks.add(info);
        }

        ResourcePackRequest.Builder builder = ResourcePackRequest.resourcePackRequest()
                .packs(resourcePacks)
                .replace(true)
                .required(PaperResources.Config.isRequired());
        String prompt = PaperResources.Config.getPrompt();
        if (!prompt.isBlank())
            builder.prompt(Component.text(prompt));

        resourceRequest = builder.build();
    }
}
