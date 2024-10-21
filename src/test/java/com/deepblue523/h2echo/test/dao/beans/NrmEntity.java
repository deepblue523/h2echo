package com.deepblue523.h2echo.test.dao.beans;

import lombok.*;

import java.sql.Timestamp;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NrmEntity {
    private Integer id;
    private String entityCode;
    private String entityName;
    private String rootEntityCode;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}