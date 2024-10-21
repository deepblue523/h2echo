package com.deepblue523.h2echo.test.dao.mapper;

import com.deepblue523.h2echo.test.dao.beans.NrmAuditResult;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NrmAuditResultsRowMapper implements RowMapper<NrmAuditResult> {
  @Override
  public NrmAuditResult mapRow(ResultSet rs, int rowNum) throws SQLException {
    return NrmAuditResult.builder()
            .id(rs.getInt("id"))
            .phoneNumberId(rs.getInt("phone_number_id"))
            .lastAuditDate(rs.getTimestamp("last_audit_date"))
            .ftcFlagged(rs.getBoolean("ftc_flagged"))
            .nomoroboFlagged(rs.getBoolean("nomorobo_flagged"))
            .robokillerFlagged(rs.getBoolean("robokiller_flagged"))
            .youmailFlagged(rs.getBoolean("youmail_flagged"))
            .mccFlagged(rs.getBoolean("mcc_flagged"))
            .ihsFlagged(rs.getBoolean("ihs_flagged"))
            .tnomoFlagged(rs.getBoolean("tnomo_flagged"))
            .ttsFlagged(rs.getBoolean("tts_flagged"))
            .teloFlagged(rs.getBoolean("telo_flagged"))
            .carrierAttFlagged(rs.getBoolean("carrier_att_flagged"))
            .carrierVerizonFlagged(rs.getBoolean("carrier_verizon_flagged"))
            .carrierTmobileFlagged(rs.getBoolean("carrier_tmobile_flagged"))
            .carrierSprintFlagged(rs.getBoolean("carrier_sprint_flagged"))
            .dateFirstFlagged(rs.getTimestamp("date_first_flagged"))
            .dateLastFlagged(rs.getTimestamp("date_last_flagged"))
            .createdAt(rs.getTimestamp("created_at"))
            .updatedAt(rs.getTimestamp("updated_at"))
            .build();
  }
}
