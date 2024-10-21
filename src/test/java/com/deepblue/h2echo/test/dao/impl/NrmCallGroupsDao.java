package com.deepblue.h2echo.test.dao.impl;

import com.deepblue.h2echo.test.dao.INrmCallGroupsDao;
import com.deepblue.h2echo.test.dao.mapper.NrmCallGroupsRowMapper;
import com.deepblue.h2echo.test.dao.beans.NrmCallGroup;
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
public class NrmCallGroupsDao implements INrmCallGroupsDao {

    final JdbcTemplate jdbcTemplate;
    private final NrmCallGroupsRowMapper rowMapper;

    // SQL templates.
    protected static final String SQL_GET_BY_ID =
            "SELECT * FROM nrm_call_groups WHERE id = ?";

    protected static final String SQL_DELETE_BY_ID =
            "DELETE FROM nrm_call_groups WHERE id = ?";

    protected static final String SQL_INSERT =
            """
            INSERT INTO nrm_call_groups (cidr_group_id, name, description)
            VALUES (?, ?, ?)
            """;

    protected static final String SQL_UPDATE_BY_ID =
            """
            UPDATE nrm_call_groups SET cidr_group_id = ?, name = ?, description = ?
             WHERE id = ?
            """;

    @Autowired
    public NrmCallGroupsDao(@Qualifier("nrmJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = new NrmCallGroupsRowMapper();
    }

    // Method to get a call group by ID
    public NrmCallGroup getById(int id) {
        return jdbcTemplate.queryForObject(SQL_GET_BY_ID, rowMapper, id);
    }

    // Method to insert a new call group
    public int insert(NrmCallGroup callGroupToInsert) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
                    ps.setInt(1, callGroupToInsert.getCidrGroupId());
                    ps.setString(2, callGroupToInsert.getName());
                    ps.setString(3, callGroupToInsert.getDescription());

                    return ps;
                },
                keyHolder);

        // Pluck and return the new key.
        if (keyHolder.getKeys() != null &&  keyHolder.getKeys().containsKey("ID")) {
            return (Integer) keyHolder.getKeys().get("ID");
        }
        else {
            throw new RuntimeException("Failed to insert call group");
        }
    }

    // Method to update a call group
    public int update(NrmCallGroup callGroup) {
        return jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(SQL_UPDATE_BY_ID);
                    ps.setInt(1, callGroup.getCidrGroupId());
                    ps.setString(2, callGroup.getName());
                    ps.setString(3, callGroup.getDescription());
                    ps.setInt(4, callGroup.getId());

                    return ps;
                });
    }

    // Method to delete a call group
    public int delete(int id) {
        return jdbcTemplate.update(SQL_DELETE_BY_ID, id);
    }
}