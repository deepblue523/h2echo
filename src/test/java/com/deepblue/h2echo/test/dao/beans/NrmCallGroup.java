package com.deepblue.h2echo.test.dao.beans;

import lombok.*;

import java.sql.Timestamp;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NrmCallGroup {
    private Integer id;
    private Integer cidrGroupId;
    private String name;
    private String description;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}