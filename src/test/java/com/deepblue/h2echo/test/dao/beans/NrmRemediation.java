package com.deepblue.h2echo.test.dao.beans;

import lombok.*;

import java.sql.Timestamp;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NrmRemediation {
    private Integer id;
    private Integer phoneNumberId;
    private Timestamp startDate;
    private Timestamp endDate;
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}