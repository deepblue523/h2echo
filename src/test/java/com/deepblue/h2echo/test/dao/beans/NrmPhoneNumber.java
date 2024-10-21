package com.deepblue.h2echo.test.dao.beans;

import lombok.*;

import java.sql.Timestamp;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NrmPhoneNumber {
    private Integer id;
    private Integer cidrId;
    private String phoneNumber;
    private String description;
    private String department;
    private String internalId;
    private String cnam;
    private Integer callGroupId;
    private Integer userId;
    private String numeracleProfileId;
    private Boolean archive;
    private Timestamp archiveDate;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}