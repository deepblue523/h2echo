package com.deepblue.h2echo.test.dao.impl;

import com.deepblue.h2echo.test.dao.INrmPhoneNumberDao;
import com.deepblue.h2echo.test.dao.mapper.NrmPhoneNumberRowMapper;
import com.deepblue.h2echo.test.dao.beans.NrmPhoneNumber;
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
public class NrmPhoneNumberDao implements INrmPhoneNumberDao {

    private final JdbcTemplate jdbcTemplate;
    private final NrmPhoneNumberRowMapper rowMapper;

    // SQL templates.
    protected static final String SQL_GET_BY_ID =
                """
                SELECT * FROM nrm_phone_numbers
                 WHERE id = ?
                """;

    protected static final String SQL_DELETE_BY_ID =
                """
                DELETE FROM nrm_phone_numbers WHERE id = ?
                """;

    protected static final String SQL_INSERT =
                """
                INSERT INTO nrm_phone_numbers (cidr_id, phone_number, description, department, internal_id, cnam, call_group_id, user_id, numeracle_profile_id, archive, archive_date)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

    protected static final String SQL_UPDATE_BY_ID =
                """
                UPDATE nrm_phone_numbers 
                   SET cidr_id = ?, phone_number = ?, description = ?, department = ?, internal_id = ?, cnam = ?, call_group_id = ?, user_id = ?, numeracle_profile_id = ?, archive = ?, archive_date = ? 
                 WHERE id = ?
                """;

    @Autowired
    public NrmPhoneNumberDao(@Qualifier("nrmJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = new NrmPhoneNumberRowMapper();
    }

    // Method to add a new phone number
    public int insert(NrmPhoneNumber recToInsert) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
                    ps.setInt(1, recToInsert.getCidrId());
                    ps.setString(2, recToInsert.getPhoneNumber());
                    ps.setString(3, recToInsert.getDescription());
                    ps.setString(4, recToInsert.getDepartment());
                    ps.setString(5, recToInsert.getInternalId());
                    ps.setString(6, recToInsert.getCnam());
                    ps.setInt(7, recToInsert.getCallGroupId());
                    ps.setInt(8, recToInsert.getUserId());
                    ps.setString(9, recToInsert.getNumeracleProfileId());
                    ps.setBoolean(10, recToInsert.getArchive());
                    ps.setTimestamp(11, recToInsert.getArchiveDate());

                    return ps;
                },
                keyHolder);

        // Pluck and return the new key.
        if (keyHolder.getKeys() != null &&  keyHolder.getKeys().containsKey("ID")) {
            return (Integer) keyHolder.getKeys().get("ID");
        }
        else {
            throw new RuntimeException("Failed to insert phone number");
        }
    }

    // Method to get a phone number by ID
    public NrmPhoneNumber getById(int id) {
        return jdbcTemplate.queryForObject(SQL_GET_BY_ID, rowMapper, id);
    }

    public int update(NrmPhoneNumber phoneNumber) {
        return jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(SQL_UPDATE_BY_ID);
                    ps.setInt(1, phoneNumber.getCidrId());
                    ps.setString(2, phoneNumber.getPhoneNumber());
                    ps.setString(3, phoneNumber.getDescription());
                    ps.setString(4, phoneNumber.getDepartment());
                    ps.setString(5, phoneNumber.getInternalId());
                    ps.setString(6, phoneNumber.getCnam());
                    ps.setInt(7, phoneNumber.getCallGroupId());
                    ps.setInt(8, phoneNumber.getUserId());
                    ps.setString(9, phoneNumber.getNumeracleProfileId());
                    ps.setBoolean(10, phoneNumber.getArchive());
                    ps.setTimestamp(11, phoneNumber.getArchiveDate());
                    ps.setInt(12, phoneNumber.getId());

                    return ps;
                });
    }

    // Method to delete a phone number by ID
    public int delete(int id) {
        return jdbcTemplate.update(SQL_DELETE_BY_ID, id);
    }
}