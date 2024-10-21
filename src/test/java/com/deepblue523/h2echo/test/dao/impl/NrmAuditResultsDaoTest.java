package com.deepblue523.h2echo.test.dao.impl;

import com.deepblue523.h2echo.annotations.EchoDao;
import com.deepblue523.h2echo.H2Echo;
import com.deepblue523.h2echo.annotations.EnableH2Echo;
import com.deepblue523.h2echo.test.dao.beans.NrmAuditResult;
import com.deepblue523.h2echo.test.dao.beans.NrmCallGroup;
import com.deepblue523.h2echo.test.dao.beans.NrmPhoneNumber;
import com.deepblue523.h2echo.test.support.TestUtils;
import com.deepblue523.h2echo.test.utils.GeneralUtils;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@EnableH2Echo(scriptPath = "SQL/migrations")
class NrmAuditResultsDaoTest {

    @EchoDao
    private NrmAuditResultsDao nrmAuditResultsDao;

    @EchoDao
    private NrmCallGroupsDao nrmCallGroupsDao;

    @EchoDao
    private NrmPhoneNumberDao nrmPhoneNumberDao;

    public NrmAuditResultsDaoTest() {
        H2Echo.echoDaosOnObject(this, true);
    }

    @Test
    void testInsert() {
        // ---[ Insert! ]---
        int newPhoneNumberId = seedRecords();

        NrmAuditResult auditResultInsert = TestUtils.getDummyNrmAuditResult(newPhoneNumberId);
        int newAuditResultId = nrmAuditResultsDao.insert(auditResultInsert);
        assertNotEquals(0, newAuditResultId);

        // ---[ Verify ]---
        NrmAuditResult auditResultReadback = nrmAuditResultsDao.getById(newAuditResultId);
        assertNotNull(auditResultReadback);

        assertTrue(GeneralUtils.compareObjectsShallow(auditResultReadback, auditResultInsert, true,
                "id", "createdAt", "updatedAt"));
    }

    @Test
    void testGetById() {
        // ---[ Insert an audit record! ]---
        NrmAuditResult auditResultInsert = TestUtils.getDummyNrmAuditResult(seedRecords());
        int newAuditResultId = nrmAuditResultsDao.insert(auditResultInsert);

        // ---[ Verify ]---
        NrmAuditResult auditResultReadback = nrmAuditResultsDao.getById(newAuditResultId);
        assertNotNull(auditResultReadback);

        auditResultInsert.setId(newAuditResultId);
    }

    @Test
    void testUpdate() {
        // ---[ Insert an audit record! ]---
        int newPhoneNumberId = seedRecords();

        NrmAuditResult auditResultInsert = new NrmAuditResult();
        auditResultInsert.setId(0);
        auditResultInsert.setPhoneNumberId(newPhoneNumberId);
        auditResultInsert.setLastAuditDate(Timestamp.from(Instant.ofEpochSecond(1000)));
        auditResultInsert.setFtcFlagged(true);
        auditResultInsert.setNomoroboFlagged(true);
        auditResultInsert.setRobokillerFlagged(true);
        auditResultInsert.setYoumailFlagged(true);
        auditResultInsert.setMccFlagged(true);
        auditResultInsert.setIhsFlagged(true);
        auditResultInsert.setTnomoFlagged(true);
        auditResultInsert.setTtsFlagged(true);
        auditResultInsert.setTeloFlagged(true);
        auditResultInsert.setCarrierAttFlagged(true);
        auditResultInsert.setCarrierVerizonFlagged(true);
        auditResultInsert.setCarrierTmobileFlagged(true);
        auditResultInsert.setCarrierSprintFlagged(true);
        auditResultInsert.setDateFirstFlagged(Timestamp.from(Instant.ofEpochSecond(2000)));
        auditResultInsert.setDateLastFlagged(Timestamp.from(Instant.ofEpochSecond(3000)));
        auditResultInsert.setCreatedAt(Timestamp.from(Instant.ofEpochSecond(4000)));
        auditResultInsert.setUpdatedAt(Timestamp.from(Instant.ofEpochSecond(5000)));

        int newAuditResultId = nrmAuditResultsDao.insert(auditResultInsert);

        // ---[ Read back and modify the record ]---
        NrmAuditResult auditResultRecToUpdate = nrmAuditResultsDao.getById(newAuditResultId);
        assertNotNull(auditResultRecToUpdate);

        auditResultRecToUpdate.setPhoneNumberId(newPhoneNumberId);
        auditResultRecToUpdate.setLastAuditDate(Timestamp.from(Instant.ofEpochSecond(8000)));
        auditResultRecToUpdate.setFtcFlagged(false);
        auditResultRecToUpdate.setNomoroboFlagged(false);
        auditResultRecToUpdate.setRobokillerFlagged(false);
        auditResultRecToUpdate.setYoumailFlagged(false);
        auditResultRecToUpdate.setMccFlagged(false);
        auditResultRecToUpdate.setIhsFlagged(false);
        auditResultRecToUpdate.setTnomoFlagged(false);
        auditResultRecToUpdate.setTtsFlagged(false);
        auditResultRecToUpdate.setTeloFlagged(false);
        auditResultRecToUpdate.setCarrierAttFlagged(false);
        auditResultRecToUpdate.setCarrierVerizonFlagged(false);
        auditResultRecToUpdate.setCarrierTmobileFlagged(false);
        auditResultRecToUpdate.setCarrierSprintFlagged(false);
        auditResultRecToUpdate.setUpdatedAt(Timestamp.from(Instant.ofEpochSecond(9000)));

        nrmAuditResultsDao.update(auditResultRecToUpdate);

        // ---[ Verify ]---
        NrmAuditResult auditResultPostUpdate = nrmAuditResultsDao.getById(newAuditResultId);
        assertTrue(GeneralUtils.compareObjectsShallow(auditResultPostUpdate, auditResultRecToUpdate, true,
                "id", "createdAt", "updatedAt"));
    }

    @Test
    void testDelete() {
        // Insert an audit record!
        NrmAuditResult auditResultInsert = TestUtils.getDummyNrmAuditResult(seedRecords());
        int newAuditResultId = nrmAuditResultsDao.insert(auditResultInsert);

        // Delete the record.
        assertEquals(1, nrmAuditResultsDao.delete(newAuditResultId));
    }

    private int seedRecords() {
        // ---[ Insert call group ]---
        NrmCallGroup callGroupRec = TestUtils.getDummyNrmCallGroup();
        int newCallGroupId = nrmCallGroupsDao.insert(callGroupRec);

        // ---[ Insert phone number and return its ID ]---
        NrmPhoneNumber phoneNumberRec = TestUtils.getDummyNrmPhoneNumber(newCallGroupId);
        return nrmPhoneNumberDao.insert(phoneNumberRec);
    }
}