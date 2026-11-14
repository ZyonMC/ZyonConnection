package dev.zyon.connection.config;

public record DatabaseSettings(String host, int port, String database, String user, String password) {

    public String jdbcUrl() {
        return "jdbc:mariadb://" + host + ":" + port + "/" + database;
    }
}
