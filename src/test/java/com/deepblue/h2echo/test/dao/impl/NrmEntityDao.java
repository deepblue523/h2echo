package com.deepblue.h2echo.test.dao.impl;

import com.deepblue.h2echo.test.dao.INrmEntityDao;
import com.deepblue.h2echo.test.dao.mapper.NrmEntityRowMapper;
import com.deepblue.h2echo.test.dao.beans.NrmEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.*;

@Slf4j
@Component
public class NrmEntityDao implements INrmEntityDao {

    private final JdbcTemplate jdbcTemplate;
    private final NrmEntityRowMapper rowMapper;

    // SQL templates.
    protected static final String SQL_GET_BY_ID =
                "SELECT * FROM nrm_entities WHERE id = ?";

    protected static final String SQL_DELETE_BY_ID =
                "DELETE FROM nrm_entities WHERE id = ?";

    protected static final String SQL_INSERT =
                """
                INSERT INTO nrm_entities (entity_code, entity_name, root_entity_code)
                VALUES (?, ?, ?)
                """;

    protected static final String SQL_UPDATE_BY_ID =
                """
                UPDATE nrm_entities 
                   SET entity_code = ?, entity_name = ?, root_entity_code = ? 
                 WHERE id = ?
                """;

    @Autowired
    public NrmEntityDao(@Qualifier("nrmJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = new NrmEntityRowMapper();
    }

    public int insert(NrmEntity entityToInsert) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, entityToInsert.getEntityCode());
                    ps.setString(2, entityToInsert.getEntityName());
                    ps.setString(3, entityToInsert.getRootEntityCode());

                    return ps;
                },
                keyHolder);

        // Pluck and return the new key.
        if (keyHolder.getKeys() != null &&  keyHolder.getKeys().containsKey("ID")) {
            return (Integer) keyHolder.getKeys().get("ID");
        }
        else {
            throw new RuntimeException("Failed to insert entity");
        }
    }

    public NrmEntity getById(int id) {
        return jdbcTemplate.queryForObject(SQL_GET_BY_ID, rowMapper, id);
    }

    public int update(NrmEntity entityToUpdate) {
        return jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(SQL_UPDATE_BY_ID);
                    ps.setString(1, entityToUpdate.getEntityCode());
                    ps.setString(2, entityToUpdate.getEntityName());
                    ps.setString(3, entityToUpdate.getRootEntityCode());
                    ps.setInt(4, entityToUpdate.getId());

                    return ps;
                });
    }

    public int delete(int id) {
        return jdbcTemplate.update(SQL_DELETE_BY_ID, id);
    }
}