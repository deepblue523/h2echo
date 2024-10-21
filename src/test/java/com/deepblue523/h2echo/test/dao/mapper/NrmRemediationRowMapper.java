package com.deepblue523.h2echo.test.dao.mapper;

import com.deepblue523.h2echo.test.dao.beans.NrmRemediation;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NrmRemediationRowMapper implements RowMapper<NrmRemediation> {
  @Override
  public NrmRemediation mapRow(ResultSet rs, int rowNum) throws SQLException {
    return NrmRemediation.builder()
    .id(rs.getInt("id"))
    .phoneNumberId(rs.getInt("phone_number_id"))
    .startDate(rs.getTimestamp("start_date"))
    .endDate(rs.getTimestamp("end_date"))
    .status(rs.getString("status"))
    .createdAt(rs.getTimestamp("created_at"))
    .updatedAt(rs.getTimestamp("updated_at"))
            .build();
  }
}
