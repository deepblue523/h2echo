package com.deepblue.h2echo.test.dao.mapper;

import com.deepblue.h2echo.test.dao.beans.NrmPhoneNumber;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NrmPhoneNumberRowMapper implements RowMapper<NrmPhoneNumber> {
  @Override
  public NrmPhoneNumber mapRow(ResultSet rs, int rowNum) throws SQLException {
    return NrmPhoneNumber.builder()
            .id(rs.getInt("id"))
            .cidrId(rs.getInt("cidr_id"))
            .phoneNumber(rs.getString("phone_number"))
            .description(rs.getString("description"))
            .department(rs.getString("department"))
            .internalId(rs.getString("internal_id"))
            .cnam(rs.getString("cnam"))
            .callGroupId(rs.getInt("call_group_id"))
            .userId(rs.getInt("user_id"))
            .numeracleProfileId(rs.getString("numeracle_profile_id"))
            .archive(rs.getBoolean("archive"))
            .archiveDate(rs.getTimestamp("archive_date"))
            .createdAt(rs.getTimestamp("created_at"))
            .updatedAt(rs.getTimestamp("updated_at"))
            .build();
  }
}
