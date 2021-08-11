package net.teamuni.rewardapi.database;

import java.sql.SQLException;
import java.util.UUID;
import javax.sql.DataSource;
import net.teamuni.rewardapi.api.Reward;
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
    public Reward[] load(UUID uuid) {
        return new Reward[0];
    }

    @Override
    public void save(UUID uuid, Reward[] rewards) {
        // TODO
    }
}
