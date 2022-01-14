package net.teamuni.rewardapi.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import net.teamuni.rewardapi.RewardAPI;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

public class SQLDatabase extends Database {

    private final DataSource sql;
    private final String tableName;

    private final String selectStatement;
    private final String insertStatement;

    public SQLDatabase(RewardAPI instance, String host, int port, String database, String tableName, String parameters, String userName, String password)
        throws SQLException {
        super(instance);

        Optional<SqlService> optional = Sponge.getServiceManager().provide(SqlService.class);
        if (!optional.isPresent()) {
            throw new RuntimeException("Failed to retrieve SqlService.");
        }

        StringBuilder sb = new StringBuilder("jdbc:mysql://").append(userName);
        if (!password.isEmpty()) {
            sb.append(":").append(password);
        }
        sb.append("@").append(host).append(":").append(port).append("/").append(database);
        if (!parameters.isEmpty()) {
            sb.append(parameters);
        }

        this.sql = optional.get().getDataSource(sb.toString());
        this.tableName = tableName;

        this.selectStatement = "SELECT reward FROM " + tableName + " WHERE uuid = '%s';";
        this.insertStatement = "INSERT INTO " + tableName + "(uuid, reward) VALUES ('%1$s', '%2$s') ON DUPLICATE KEY UPDATE reward = '%2$s';";

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
}
