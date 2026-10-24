/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.util.HexFormat
 *  net.kyori.adventure.resource.ResourcePackInfo
 *  net.kyori.adventure.resource.ResourcePackInfoLike
 *  net.kyori.adventure.resource.ResourcePackRequest
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 *  org.bukkit.entity.Player
 */
package de.nick.casino;

import com.sun.net.httpserver.HttpServer;
import de.nick.casino.CasinoPlugin;
import de.nick.casino.util.Bedrock;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackInfoLike;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

public final class PackServer {
    private static final UUID PACK_ID = UUID.nameUUIDFromBytes("minevegas-casino-pack".getBytes(StandardCharsets.UTF_8));
    private final CasinoPlugin plugin;
    private File packFile;
    private byte[] packBytes;
    private String sha1;
    private HttpServer server;
    private ExecutorService executor;

    public PackServer(CasinoPlugin casinoPlugin) {
        this.plugin = casinoPlugin;
    }

    public void start() throws Exception {
        this.packFile = new File(this.plugin.getDataFolder(), "casino-pack.zip");
        this.plugin.getDataFolder().mkdirs();
        try (InputStream inputStream = this.plugin.getResource("casino-pack.zip");){
            if (inputStream == null) {
                throw new IllegalStateException("casino-pack.zip fehlt im Plugin");
            }
            Files.copy(inputStream, this.packFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        this.packBytes = Files.readAllBytes(this.packFile.toPath());
        this.sha1 = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(this.packBytes));
        int n = this.plugin.getConfig().getInt("pack.port", 8099);
        String string = this.plugin.getConfig().getString("pack.bind", "0.0.0.0");
        this.server = HttpServer.create(new InetSocketAddress(string, n), 16);
        this.server.createContext("/", httpExchange -> {
            try {
                String string = httpExchange.getRequestURI().getPath();
                if (!string.startsWith("/casino-pack") || !"GET".equalsIgnoreCase(httpExchange.getRequestMethod()) && !"HEAD".equalsIgnoreCase(httpExchange.getRequestMethod())) {
                    httpExchange.sendResponseHeaders(404, -1L);
                    return;
                }
                httpExchange.getResponseHeaders().add("Content-Type", "application/zip");
                httpExchange.getResponseHeaders().add("Cache-Control", "no-cache");
                if ("HEAD".equalsIgnoreCase(httpExchange.getRequestMethod())) {
                    httpExchange.sendResponseHeaders(200, -1L);
                    return;
                }
                httpExchange.sendResponseHeaders(200, this.packBytes.length);
                try (OutputStream outputStream = httpExchange.getResponseBody();){
                    outputStream.write(this.packBytes);
                }
            }
            finally {
                httpExchange.close();
            }
        });
        this.executor = Executors.newFixedThreadPool(4, runnable -> {
            Thread thread = new Thread(runnable, "Casino-Pack-HTTP");
            thread.setDaemon(true);
            return thread;
        });
        this.server.setExecutor(this.executor);
        this.server.start();
        this.plugin.getLogger().info("Ressourcenpaket wird auf Port " + n + " bereitgestellt (SHA-1 " + this.sha1 + ", " + this.packBytes.length / 1024 + " KB).");
        this.plugin.getLogger().info("Oeffentliche Adresse: " + this.url());
    }

    public void stop() {
        if (this.server != null) {
            this.server.stop(0);
        }
        if (this.executor != null) {
            this.executor.shutdownNow();
        }
    }

    public String url() {
        String string = this.plugin.getConfig().getString("pack.public-url", "http://127.0.0.1:8099");
        while (string.endsWith("/")) {
            string = string.substring(0, string.length() - 1);
        }
        return string + "/casino-pack-" + this.sha1.substring(0, 12) + ".zip";
    }

    public void send(Player player) {
        if (Bedrock.isBedrock(player) || this.sha1 == null) {
            return;
        }
        ResourcePackInfo resourcePackInfo = ResourcePackInfo.resourcePackInfo((UUID)PACK_ID, (URI)URI.create(this.url()), (String)this.sha1);
        String string = this.plugin.getConfig().getString("pack.prompt", "Casino-Texturen und -Men\u00fcs");
        player.sendResourcePacks((ResourcePackRequest)ResourcePackRequest.resourcePackRequest().packs((ResourcePackInfoLike)resourcePackInfo, new ResourcePackInfoLike[0]).required(this.plugin.getConfig().getBoolean("pack.required", true)).replace(false).prompt((Component)Component.text((String)string, (TextColor)NamedTextColor.GOLD)).build());
    }
}

