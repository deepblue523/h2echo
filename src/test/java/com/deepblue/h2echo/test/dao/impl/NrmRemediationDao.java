package com.deepblue.h2echo.test.dao.impl;

import com.deepblue.h2echo.test.dao.INrmRemediationDao;
import com.deepblue.h2echo.test.dao.mapper.NrmRemediationRowMapper;
import com.deepblue.h2echo.test.dao.beans.NrmRemediation;
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
public class NrmRemediationDao implements INrmRemediationDao {
    private final JdbcTemplate jdbcTemplate;
    private final NrmRemediationRowMapper rowMapper;

    // SQL templates.
    protected static final String SQL_GET_BY_ID =
                """
                SELECT * FROM nrm_remediations WHERE id = ?
                """;

    protected static final String SQL_DELETE_BY_ID =
                "DELETE FROM nrm_remediations WHERE id = ?";

    protected static final String SQL_INSERT =
                """
                INSERT INTO nrm_remediations (phone_number_id, start_date, end_date, status)
                VALUES (?, ?, ?, ?)
                """;

    protected static final String SQL_UPDATE_BY_ID =
                """
                UPDATE nrm_remediations
                   SET phone_number_id = ?, start_date = ?, end_date = ?, status = ?
                 WHERE id = ?
                """;

    @Autowired
    public NrmRemediationDao(@Qualifier("nrmJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = new NrmRemediationRowMapper();
    }

    public int insert(NrmRemediation recToInsert) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
                    ps.setInt(1, recToInsert.getPhoneNumberId());
                    ps.setTimestamp(2, recToInsert.getStartDate());
                    ps.setTimestamp(3, recToInsert.getEndDate());
                    ps.setString(4, recToInsert.getStatus());
                    ps.executeUpdate();

                    return ps;
                },
                keyHolder);

        // Pluck and return the new key.
        if (keyHolder.getKeys() != null &&  keyHolder.getKeys().containsKey("ID")) {
            return (Integer) keyHolder.getKeys().get("ID");
        }
        else {
            throw new RuntimeException("Failed to insert remediation");
        }
    }

    public NrmRemediation getById(int id) {
        return jdbcTemplate.queryForObject(SQL_GET_BY_ID, rowMapper, id);
    }

    public int update(NrmRemediation remediation) {
        return jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(SQL_UPDATE_BY_ID);
                    ps.setInt(1, remediation.getPhoneNumberId());
                    ps.setTimestamp(2, remediation.getStartDate());
                    ps.setTimestamp(3, remediation.getEndDate());
                    ps.setString(4, remediation.getStatus());
                    ps.setInt(5, remediation.getId());

                    return ps;
                });
    }

    public int delete(int id) {
        return jdbcTemplate.update(SQL_DELETE_BY_ID, id);
    }
}