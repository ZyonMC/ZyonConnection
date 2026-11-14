package dev.zyon.connection.storage;

import dev.zyon.connection.config.DatabaseSettings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Acceso a MariaDB. Metodos bloqueantes: llamarlos fuera del hilo de red. */
public final class LastServerRepository {

    private final DatabaseSettings settings;

    public LastServerRepository(DatabaseSettings settings) {
        this.settings = settings;
    }

    /** Crea la tabla; falla rapido si la base de datos no es accesible. */
    public void init() throws SQLException, ClassNotFoundException {
        Class.forName("org.mariadb.jdbc.Driver");
        try (Connection c = connect(); Statement st = c.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS last_server ("
                    + "uuid CHAR(36) NOT NULL, grp VARCHAR(64) NOT NULL, server VARCHAR(64) NOT NULL, "
                    + "PRIMARY KEY (uuid, grp))");
        }
    }

    /** @return ultimo servidor por nombre de grupo */
    public Map<String, String> findAll(UUID player) throws SQLException {
        Map<String, String> result = new HashMap<>();
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("SELECT grp, server FROM last_server WHERE uuid = ?")) {
            ps.setString(1, player.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("grp"), rs.getString("server"));
                }
            }
        }
        return result;
    }

    public void save(UUID player, String group, String server) throws SQLException {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO last_server (uuid, grp, server) VALUES (?, ?, ?) "
                             + "ON DUPLICATE KEY UPDATE server = VALUES(server)")) {
            ps.setString(1, player.toString());
            ps.setString(2, group);
            ps.setString(3, server);
            ps.executeUpdate();
        }
    }

    // ponytail: una conexion por operacion; HikariCP (shadeado) si la carga lo pide.
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(settings.jdbcUrl(), settings.user(), settings.password());
    }
}
