package net.teamuni.rewardapi.database;

import java.sql.SQLException;
import java.util.UUID;
import javax.sql.DataSource;
import net.teamuni.rewardapi.api.Reward;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

public class SQLDatabase implements Database {

    private SqlService sql;

    private DataSource getDataSource(String jdbcUrl) throws SQLException {
        if (sql == null) {
            sql = Sponge.getServiceManager().provide(SqlService.class).get();
        }
        return sql.getDataSource(jdbcUrl);
    }

    @Override
    public @NonNull Reward[] load(@NonNull UUID uuid) {
        return new Reward[0];
    }

    @Override
    public void save(@NonNull UUID uuid, @NonNull Reward[] rewards) {
        // TODO
    }
}
