package net.teamuni.rewardapi.data.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import javax.sql.DataSource;
import net.teamuni.rewardapi.RewardAPI;
import net.teamuni.rewardapi.data.object.Reward;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SQLDatabase extends Database {

    private final static String REWARDS_TABLE = "rewardapi_rewards";
    private final static String LOG_RECEIVED_TABLE = "rewardapi_log_received";
    private final static String LOG_CLAIMED_TABLE = "rewardapi_log_claimed";

    private final DataSource sql;
    private final String tableName;

    private final String selectStatement;
    private final String insertStatement;

    public SQLDatabase(RewardAPI instance, String host, int port, String database, String tableName,
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
        this.tableName = tableName;

        this.selectStatement = "SELECT reward FROM " + REWARDS_TABLE + " WHERE uuid = '%s';";
        this.insertStatement = "INSERT INTO " + REWARDS_TABLE
            + "(uuid, reward) VALUES ('%1$s', '%2$s') ON DUPLICATE KEY UPDATE reward = '%2$s';";

        initTable();
    }

    private Connection getConnection() throws SQLException {
        return sql.getConnection();
    }

    private void initTable() {
        try (Connection con = getConnection();
            Statement stmt = con.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS " + tableName
                    + "("
                    + "    uuid   CHAR(36),"
                    + "    reward JSON NOT NULL,"
                    + "    PRIMARY KEY (uuid)"
                    + ");"
            );
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
    protected long logReceived(UUID uuid, String json) {
        return 0;
    }

    @Override
    protected void logClaimed(UUID uuid, long receivedLogId) {

    }
}
