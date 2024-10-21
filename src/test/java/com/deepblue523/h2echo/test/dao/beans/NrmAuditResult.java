package com.deepblue523.h2echo.test.dao.beans;

import lombok.*;

import java.sql.Timestamp;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NrmAuditResult {
    Integer id;
    Integer phoneNumberId;
    Timestamp lastAuditDate;
    Boolean ftcFlagged;
    Boolean nomoroboFlagged;
    Boolean robokillerFlagged;
    Boolean youmailFlagged;
    Boolean mccFlagged;
    Boolean ihsFlagged;
    Boolean tnomoFlagged;
    Boolean ttsFlagged;
    Boolean teloFlagged;
    Boolean carrierAttFlagged;
    Boolean carrierVerizonFlagged;
    Boolean carrierTmobileFlagged;
    Boolean carrierSprintFlagged;
    Timestamp dateFirstFlagged;
    Timestamp dateLastFlagged;
    Timestamp createdAt;
    Timestamp updatedAt;
}