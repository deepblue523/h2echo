package com.deepblue523.h2echo.test.dao.impl;

import com.deepblue523.h2echo.test.dao.INrmAuditResultsDao;
import com.deepblue523.h2echo.test.dao.mapper.NrmAuditResultsRowMapper;
import com.deepblue523.h2echo.test.dao.beans.NrmAuditResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.*;

@Slf4j
@Component
public class NrmAuditResultsDao implements INrmAuditResultsDao {

    final JdbcTemplate jdbcTemplate;
    private final NrmAuditResultsRowMapper rowMapper;

    // SQL templates.
    //@VisibleForTesting
    protected static final String SQL_GET_BY_ID =
            "SELECT * FROM nrm_audit_results WHERE id = ?";

    protected static final String SQL_DELETE_BY_ID =
            "DELETE FROM nrm_audit_results WHERE id = ?";

    protected static final String SQL_INSERT =
            """
            INSERT INTO nrm_audit_results (phone_number_id, last_audit_date, ftc_flagged, nomorobo_flagged, 
            robokiller_flagged, youmail_flagged, mcc_flagged, ihs_flagged, tnomo_flagged, tts_flagged, 
            telo_flagged, carrier_att_flagged, carrier_verizon_flagged, carrier_tmobile_flagged, 
            carrier_sprint_flagged, date_first_flagged, date_last_flagged) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    protected static final String SQL_UPDATE_BY_ID =
            """
            UPDATE nrm_audit_results SET phone_number_id = ?, last_audit_date = ?, ftc_flagged = ?,
            nomorobo_flagged = ?, robokiller_flagged = ?, youmail_flagged = ?, mcc_flagged = ?, ihs_flagged = ?,
            tnomo_flagged = ?, tts_flagged = ?, telo_flagged = ?, carrier_att_flagged = ?,
            carrier_verizon_flagged = ?, carrier_tmobile_flagged = ?, carrier_sprint_flagged = ?,
            date_first_flagged = ?, date_last_flagged = ? 
            WHERE id = ?
            """;

    @Autowired
    public NrmAuditResultsDao(@Qualifier("nrmJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = new NrmAuditResultsRowMapper();
    }

    public int insert(NrmAuditResult auditRecToInsert) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
                    ps.setInt(1, auditRecToInsert.getPhoneNumberId());
                    ps.setTimestamp(2, auditRecToInsert.getLastAuditDate());
                    ps.setBoolean(3, auditRecToInsert.getFtcFlagged());
                    ps.setBoolean(4, auditRecToInsert.getNomoroboFlagged());
                    ps.setBoolean(5, auditRecToInsert.getRobokillerFlagged());
                    ps.setBoolean(6, auditRecToInsert.getYoumailFlagged());
                    ps.setBoolean(7, auditRecToInsert.getMccFlagged());
                    ps.setBoolean(8, auditRecToInsert.getIhsFlagged());
                    ps.setBoolean(9, auditRecToInsert.getTnomoFlagged());
                    ps.setBoolean(10, auditRecToInsert.getTtsFlagged());
                    ps.setBoolean(11, auditRecToInsert.getTeloFlagged());
                    ps.setBoolean(12, auditRecToInsert.getCarrierAttFlagged());
                    ps.setBoolean(13, auditRecToInsert.getCarrierVerizonFlagged());
                    ps.setBoolean(14, auditRecToInsert.getCarrierTmobileFlagged());
                    ps.setBoolean(15, auditRecToInsert.getCarrierSprintFlagged());
                    ps.setTimestamp(16, auditRecToInsert.getDateFirstFlagged());
                    ps.setTimestamp(17, auditRecToInsert.getDateLastFlagged());

                    return ps;
                },
                keyHolder);

        // Pluck and return the new key.
        if (keyHolder.getKeys() != null &&  keyHolder.getKeys().containsKey("ID")) {
            return (Integer) keyHolder.getKeys().get("ID");
        }
        else {
            throw new RuntimeException("Failed to insert audit results");
        }
    }

    public NrmAuditResult getById(int id) {
        try {
            return jdbcTemplate.queryForObject(SQL_GET_BY_ID, rowMapper, id);
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public int update(NrmAuditResult auditRecToUpdate) {
        return
            jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(SQL_UPDATE_BY_ID);
                    ps.setInt(1, auditRecToUpdate.getPhoneNumberId());
                    ps.setTimestamp(2, auditRecToUpdate.getLastAuditDate());
                    ps.setBoolean(3, auditRecToUpdate.getFtcFlagged());
                    ps.setBoolean(4, auditRecToUpdate.getNomoroboFlagged());
                    ps.setBoolean(5, auditRecToUpdate.getRobokillerFlagged());
                    ps.setBoolean(6, auditRecToUpdate.getYoumailFlagged());
                    ps.setBoolean(7, auditRecToUpdate.getMccFlagged());
                    ps.setBoolean(8, auditRecToUpdate.getIhsFlagged());
                    ps.setBoolean(9, auditRecToUpdate.getTnomoFlagged());
                    ps.setBoolean(10, auditRecToUpdate.getTtsFlagged());
                    ps.setBoolean(11, auditRecToUpdate.getTeloFlagged());
                    ps.setBoolean(12, auditRecToUpdate.getCarrierAttFlagged());
                    ps.setBoolean(13, auditRecToUpdate.getCarrierVerizonFlagged());
                    ps.setBoolean(14, auditRecToUpdate.getCarrierTmobileFlagged());
                    ps.setBoolean(15, auditRecToUpdate.getCarrierSprintFlagged());
                    ps.setTimestamp(16, auditRecToUpdate.getDateFirstFlagged());
                    ps.setTimestamp(17, auditRecToUpdate.getDateLastFlagged());
                    ps.setInt(18, auditRecToUpdate.getId());

                    return ps;
                });
    }

    public int delete(int id) {
        return jdbcTemplate.update(SQL_DELETE_BY_ID, id);
    }
}