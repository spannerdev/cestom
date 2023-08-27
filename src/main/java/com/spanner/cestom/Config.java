package com.spanner.cestom;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;

public class Config {

    private static final File PROPERTIES_FILE = new File("cestom.properties");

    private final Properties properties;
    private static Config INSTANCE = null;
    private Config(Properties properties) {
        this.properties = properties;

        assert assertConfig();

        INSTANCE = this;

        setSystemProperties();
    }

    public static void load() {
        if (!PROPERTIES_FILE.exists()) {
            createConfigFile();
        }
        try {
            tryLoad();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static Config tryLoad() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(PROPERTIES_FILE));
        Properties properties = new Properties();

        properties.load(reader);
        return new Config(properties);
    }
    private static void createConfigFile() {
        try {
            InputStream is = Config.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE.getName());
            assert is != null : "Configuration resource is null!";
            Files.copy(is, Paths.get(PROPERTIES_FILE.getName()));
        } catch (IOException e) {
            throw new RuntimeException("Could not create config properties file", e);
        }
    }

    public static void setSystemProperties() {
        System.setProperty("minestom.tps", Config.getTpsString());
        System.setProperty("minestom.chunk-view-distance", Config.getChunkViewDistanceString());
        System.setProperty("minestom.entity-view-distance", Config.getEntityViewDistanceString());
        System.setProperty("minestom.use-new-chunk-sending", Config.getUseNewChunkSendingString());
    }

    private static Config getInstance() {
        assert INSTANCE != null : "Config was accessed before being loaded!";
        return INSTANCE;
    }
    private Properties properties() {
        return properties;
    }
    private boolean assertConfig() {
        return true;
    }


    private static String getHostString() {
        return getInstance().properties().getProperty("server.listen.host");
    }
    private static String getPortString() {
        return getInstance().properties().getProperty("server.listen.port");
    }
    private static String getModeString() {
        return getInstance().properties().getProperty("server.mode");
    }
    private static String getTpsString() {
        return getInstance().properties().getProperty("server.tps");
    }
    private static String getChunkViewDistanceString() {
        return getInstance().properties().getProperty("server.view_distance.chunk");
    }
    private static String getEntityViewDistanceString() {
        return getInstance().properties().getProperty("server.view_distance.entity");
    }
    private static String getVelocitySecretString() {
        return getInstance().properties().getProperty("velocity.secret");
    }
    private static String getUseNewChunkSendingString() {
        return getInstance().properties().getProperty("improvements.chunk-sending");
    }

    public static InetSocketAddress getListenAddress() {
        return new InetSocketAddress(getHost(), getPort());
    }
    public static String getHost() {
        return getHostString();
    }
    public static int getPort() {
        return Integer.parseInt(getPortString());
    }
    public static RunMode getMode() {
        return RunMode.valueOf(getModeString().toUpperCase(Locale.ENGLISH));
    }
    public static int getTps() {
        return Integer.parseInt(getTpsString());
    }
    public static int getChunkViewDistance() {
        return Integer.parseInt(getChunkViewDistanceString());
    }
    public static int getEntityViewDistance() {
        return Integer.parseInt(getEntityViewDistanceString());
    }
    protected static String getVelocitySecret() {
        return getVelocitySecretString();
    }
    private static boolean getUseNewChunkSending() {
        return Boolean.parseBoolean(getUseNewChunkSendingString());
    }


    public enum RunMode {
        OFFLINE
        ,ONLINE
        ,BUNGEECORD
        ,VELOCITY
        ;
    }

}
