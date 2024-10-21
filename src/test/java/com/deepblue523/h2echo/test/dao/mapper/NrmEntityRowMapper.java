package com.deepblue523.h2echo.test.dao.mapper;

import com.deepblue523.h2echo.test.dao.beans.NrmEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NrmEntityRowMapper implements RowMapper<NrmEntity> {
  @Override
  public NrmEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
    return NrmEntity.builder()
            .id(rs.getInt("id"))
            .entityCode(rs.getString("entity_code"))
            .entityName(rs.getString("entity_name"))
            .rootEntityCode(rs.getString("root_entity_code"))
            .createdAt(rs.getTimestamp("created_at"))
            .updatedAt(rs.getTimestamp("updated_at"))
            .build();
  }
}
