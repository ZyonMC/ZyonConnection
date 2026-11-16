package net.zyonmc.zyonconnection.config;

import net.zyonmc.zyonconnection.group.Group;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/** Configuracion cargada de {@code config.yml}. */
public record Settings(DatabaseSettings database, List<Group> groups) {

    public static Settings load(File dataFolder, ClassLoader resources) throws IOException {
        File file = new File(dataFolder, "config.yml");
        if (!file.exists()) {
            Files.createDirectories(dataFolder.toPath());
            try (InputStream defaults = resources.getResourceAsStream("config.yml")) {
                Files.copy(defaults, file.toPath());
            }
        }
        Configuration yaml = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        return new Settings(readDatabase(yaml.getSection("mariadb")), readGroups(yaml.getSection("groups")));
    }

    private static DatabaseSettings readDatabase(Configuration section) {
        return new DatabaseSettings(
                section.getString("host"),
                section.getInt("port"),
                section.getString("database"),
                section.getString("user"),
                section.getString("password"));
    }

    private static List<Group> readGroups(Configuration section) {
        List<Group> groups = new ArrayList<>();
        for (String name : section.getKeys()) {
            groups.add(new Group(name, section.getString(name + ".main"), section.getStringList(name + ".servers")));
        }
        return groups;
    }
}
