package net.teamuni.rewardapi.data.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import javax.sql.DataSource;
import net.teamuni.rewardapi.RewardAPI;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SQLDatabase extends Database {

    private final static String REWARDS_TABLE = "rewardapi_rewards";
    private final static String LOG_RECEIVED_TABLE = "rewardapi_log_received";
    private final static String LOG_CLAIMED_TABLE = "rewardapi_log_claimed";

    private final DataSource sql;

    private final String selectStatement;
    private final String insertStatement;
    private final String logReceivedStatement;
    private final String logClaimedStatement;

    public SQLDatabase(RewardAPI instance, String host, int port, String database,
        String parameters, String userName, String password)
        throws SQLException {
        super(instance);

        HikariConfig config = new HikariConfig();
        config.setUsername(userName);
        config.setPassword(password);
        StringBuilder sb = new StringBuilder("jdbc:mysql://")
            .append(host).append(":").append(port).append("/").append(database);
        if (!parameters.isEmpty()) {
            sb.append(parameters);
        }
        config.setJdbcUrl(sb.toString());

        this.sql = new HikariDataSource(config);

        this.selectStatement = "SELECT reward FROM " + REWARDS_TABLE + " WHERE uuid = '%s';";
        this.insertStatement = "INSERT INTO " + REWARDS_TABLE
            + "(uuid, reward) VALUES ('%1$s', '%2$s') ON DUPLICATE KEY UPDATE reward = '%2$s';";

        this.logReceivedStatement =
            "INSERT INTO " + LOG_RECEIVED_TABLE + " (player, reward) VALUES (?, ?);";
        this.logClaimedStatement =
            "INSERT INTO " + LOG_CLAIMED_TABLE + " (player, received_log_id) VALUES (?, ?);";

        initTable();
    }

    private static boolean existsIndex(Connection conn, String table, String indexName)
        throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT COUNT(1) indexExists FROM INFORMATION_SCHEMA.STATISTICS\n"
                    + "WHERE table_schema=DATABASE() AND table_name='" + table
                    + "' AND index_name='"
                    + indexName + "';");
            try (rs) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        }
        return false;
    }

    private Connection getConnection() throws SQLException {
        return sql.getConnection();
    }

    private void initTable() {
        try (Connection con = getConnection();
            Statement stmt = con.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS " + REWARDS_TABLE
                    + "("
                    + "    uuid   CHAR(36),"
                    + "    reward JSON NOT NULL,"
                    + "    PRIMARY KEY (uuid)"
                    + ");"
            );
            stmt.execute("CREATE TABLE IF NOT EXISTS " + LOG_RECEIVED_TABLE
                + "("
                + "    id     INT UNSIGNED NOT NULL AUTO_INCREMENT,"
                + "    player CHAR(36)     NOT NULL,"
                + "    reward JSON         NOT NULL,"
                + "    time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "    PRIMARY KEY (id)"
                + ");");
            if (!existsIndex(con, LOG_RECEIVED_TABLE, "idx_player")) {
                stmt.execute("CREATE INDEX idx_player ON " + LOG_RECEIVED_TABLE + " (player);");
            }
            stmt.execute("CREATE TABLE IF NOT EXISTS " + LOG_CLAIMED_TABLE
                + "("
                + "    id              INT UNSIGNED NOT NULL AUTO_INCREMENT,"
                + "    player          CHAR(36)     NOT NULL,"
                + "    received_log_id INT          NOT NULL,"
                + "    time            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "    PRIMARY KEY (id)"
                + ");");
            if (!existsIndex(con, LOG_RECEIVED_TABLE, "idx_player")) {
                stmt.execute("CREATE INDEX idx_player ON " + LOG_RECEIVED_TABLE + " (player);");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected @NonNull String loadJson(@NonNull UUID uuid) throws SQLException {
        try (Connection con = getConnection();
            Statement stmt = con.createStatement();
            ResultSet resultSet = stmt.executeQuery(String.format(selectStatement, uuid))) {
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
            return "";
        }
    }

    @Override
    protected void saveJson(@NonNull UUID uuid, @NonNull String json) throws SQLException {
        try (Connection con = getConnection();
            Statement stmt = con.createStatement()) {
            stmt.execute(String.format(insertStatement, uuid, json));
        }
    }

    @Override
    protected long logReceived(UUID uuid, String json) throws SQLException {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(
            this.logReceivedStatement, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, json);
            ps.execute();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return -1;
    }

    @Override
    protected void logClaimed(UUID uuid, long receivedLogId) throws SQLException {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(
            this.logClaimedStatement)) {
            ps.setString(1, uuid.toString());
            ps.setLong(2, receivedLogId);
            ps.execute();
        }
    }
}
