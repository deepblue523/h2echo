package com.deepblue523.h2echo.test.dao.mapper;

import com.deepblue523.h2echo.test.dao.beans.NrmCallGroup;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NrmCallGroupsRowMapper implements RowMapper<NrmCallGroup> {
  @Override
  public NrmCallGroup mapRow(ResultSet rs, int rowNum) throws SQLException {
    return NrmCallGroup.builder()
            .id(rs.getInt("id"))
            .cidrGroupId(rs.getInt("cidr_group_id"))
            .name(rs.getString("name"))
            .description(rs.getString("description"))
            .createdAt(rs.getTimestamp("created_at"))
            .updatedAt(rs.getTimestamp("updated_at"))
            .build();
  }
}
