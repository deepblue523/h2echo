package com.deepblue523.h2echo.test.support;

import com.deepblue523.h2echo.test.dao.beans.NrmAuditResult;
import com.deepblue523.h2echo.test.dao.beans.NrmCallGroup;
import com.deepblue523.h2echo.test.dao.beans.NrmEntity;
import com.deepblue523.h2echo.test.dao.beans.NrmPhoneNumber;

import java.sql.Timestamp;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestUtils {
    public static NrmAuditResult getDummyNrmAuditResult(int phoneNumberId) {
        NrmAuditResult auditResult = new NrmAuditResult();

        auditResult.setId(0);
        auditResult.setPhoneNumberId(phoneNumberId);
        auditResult.setLastAuditDate(new Timestamp(System.currentTimeMillis()));
        auditResult.setFtcFlagged(true);
        auditResult.setNomoroboFlagged(true);
        auditResult.setRobokillerFlagged(true);
        auditResult.setYoumailFlagged(true);
        auditResult.setMccFlagged(true);
        auditResult.setIhsFlagged(true);
        auditResult.setTnomoFlagged(true);
        auditResult.setTtsFlagged(true);
        auditResult.setTeloFlagged(true);
        auditResult.setCarrierAttFlagged(true);
        auditResult.setCarrierVerizonFlagged(true);
        auditResult.setCarrierTmobileFlagged(true);
        auditResult.setCarrierSprintFlagged(true);
        auditResult.setDateFirstFlagged(new Timestamp(System.currentTimeMillis()));
        auditResult.setDateLastFlagged(new Timestamp(System.currentTimeMillis()));

        return auditResult;
    }

    public static NrmCallGroup getDummyNrmCallGroup() {
        NrmCallGroup dummyCallGroup = new NrmCallGroup();

        dummyCallGroup.setId(0);
        dummyCallGroup.setCidrGroupId(100);
        dummyCallGroup.setName("Sample Call Group");
        dummyCallGroup.setDescription("This is a dummy call group for testing purposes");
        dummyCallGroup.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        dummyCallGroup.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        return dummyCallGroup;
    }

    public static NrmEntity getDummyNrmEntity() {
        NrmEntity dummyEntity = new NrmEntity();

        dummyEntity.setId(0);
        dummyEntity.setEntityCode("ENT-001");
        dummyEntity.setEntityName("Sample Entity");
        dummyEntity.setRootEntityCode("ROOT-001");
        dummyEntity.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        dummyEntity.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        return dummyEntity;
    }

    public static NrmPhoneNumber getDummyNrmPhoneNumber(int callGgroupId) {
        NrmPhoneNumber dummyPhoneNumber = new NrmPhoneNumber();

        Random newRnd = new Random();
        String rndPhoneNumber = "+1555" + String.format("%07d", newRnd.nextInt(10000000));

        dummyPhoneNumber.setId(0);
        dummyPhoneNumber.setCidrId(100);
        dummyPhoneNumber.setPhoneNumber(rndPhoneNumber);
        dummyPhoneNumber.setDescription("Main Office Reception");
        dummyPhoneNumber.setDepartment("Customer Service");
        dummyPhoneNumber.setInternalId("CS001");
        dummyPhoneNumber.setCnam("ACME Corp");
        dummyPhoneNumber.setCallGroupId(callGgroupId);
        dummyPhoneNumber.setUserId(42);
        dummyPhoneNumber.setNumeracleProfileId("NP-789");
        dummyPhoneNumber.setArchive(false);
        dummyPhoneNumber.setArchiveDate(null);
        dummyPhoneNumber.setCreatedAt(new Timestamp(System.currentTimeMillis() - 86400000)); // 1 day ago
        dummyPhoneNumber.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        return dummyPhoneNumber;
    }
}
