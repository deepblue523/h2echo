package com.deepblue.h2echo.test.dao.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.deepblue.h2echo.annotations.EchoDao;
import com.deepblue.h2echo.H2Echo;
import com.deepblue.h2echo.test.dao.beans.NrmCallGroup;
import com.deepblue.h2echo.test.dao.beans.NrmPhoneNumber;
import com.deepblue.h2echo.test.dao.beans.NrmRemediation;
import com.deepblue.h2echo.test.utils.GeneralUtils;
import com.deepblue.h2echo.testsupport.TestUtils;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.HashMap;

class NrmRemediationDaoTest {

    @EchoDao private NrmRemediationDao nrmRemediationDao;
    @EchoDao private NrmPhoneNumberDao nrmPhoneNumberDao;
    @EchoDao private NrmCallGroupsDao nrmCallGroupsDao;

    public NrmRemediationDaoTest() {
        H2Echo.echoDaosOnObject(this, true);
    }

    @Test
    void testInsertAndReadback() {
        // ---[ Insert! ]---
        NrmCallGroup callGroupPreInsert = TestUtils.getDummyNrmCallGroup();
        int newCallGroupId = nrmCallGroupsDao.insert(callGroupPreInsert);

        NrmPhoneNumber phoneNumber = TestUtils.getDummyNrmPhoneNumber(newCallGroupId);
        int newPhoneNumberId = nrmPhoneNumberDao.insert(phoneNumber);

        NrmRemediation remediationToInsert = new NrmRemediation();
        remediationToInsert.setId(0);
        remediationToInsert.setPhoneNumberId(newPhoneNumberId);
        remediationToInsert.setStatus("XYZ");
        remediationToInsert.setStartDate(new Timestamp(System.currentTimeMillis()));
        remediationToInsert.setEndDate(new Timestamp(System.currentTimeMillis() + 10000));
        int newRemediationId = nrmRemediationDao.insert(remediationToInsert);

        // ---[ Read it back ]---
        NrmRemediation remediationReadback = nrmRemediationDao.getById(newRemediationId);
        assertNotNull(remediationReadback);

        // ---[ Verify ]---
        assertTrue(GeneralUtils.compareObjectsShallow(
                remediationReadback, remediationToInsert, true, "id", "createdAt", "updatedAt"));
    }

    @Test
    void testUpdateById() {
        // ---[ Insert! ]---
        NrmCallGroup callGroupPreInsert = TestUtils.getDummyNrmCallGroup();
        int newCallGroupId = nrmCallGroupsDao.insert(callGroupPreInsert);

        NrmPhoneNumber phoneNumber = TestUtils.getDummyNrmPhoneNumber(newCallGroupId);
        int newPhoneNumberId = nrmPhoneNumberDao.insert(phoneNumber);

        NrmRemediation remediationToInsert = new NrmRemediation();
        remediationToInsert.setId(0);
        remediationToInsert.setPhoneNumberId(newPhoneNumberId);
        remediationToInsert.setStatus("XYZ");
        remediationToInsert.setStartDate(new Timestamp(System.currentTimeMillis()));
        remediationToInsert.setEndDate(new Timestamp(System.currentTimeMillis() + 10000));
        int newRemediationId = nrmRemediationDao.insert(remediationToInsert);

        // ---[ Modify and verify the update ]---
        NrmRemediation remediationPostInsert = nrmRemediationDao.getById(newRemediationId);
        assertNotNull(remediationPostInsert);

        remediationPostInsert.setStatus("ABC");
        remediationPostInsert.setStartDate(new Timestamp(System.currentTimeMillis()));
        remediationPostInsert.setEndDate(new Timestamp(System.currentTimeMillis() + 10000));
        nrmRemediationDao.update(remediationPostInsert);

        // ---[ Verify ]---
        NrmRemediation remediationReadback = nrmRemediationDao.getById(newRemediationId);

        assertNotNull(remediationReadback);
        assertTrue(GeneralUtils.compareObjectsShallow(
                remediationReadback, remediationPostInsert, true, "id", "createdAt", "updatedAt"));


    }

    @Test
    void testDeleteById() {
        // ---[ Insert! ]---
        NrmCallGroup callGroupPreInsert = TestUtils.getDummyNrmCallGroup();
        int newCallGroupId = nrmCallGroupsDao.insert(callGroupPreInsert);

        NrmPhoneNumber phoneNumber = TestUtils.getDummyNrmPhoneNumber(newCallGroupId);
        int newPhoneNumberId = nrmPhoneNumberDao.insert(phoneNumber);

        NrmRemediation remediationToInsert = new NrmRemediation();
        remediationToInsert.setId(0);
        remediationToInsert.setPhoneNumberId(newPhoneNumberId);
        remediationToInsert.setStatus("XYZ");
        remediationToInsert.setStartDate(new Timestamp(System.currentTimeMillis()));
        remediationToInsert.setEndDate(new Timestamp(System.currentTimeMillis() + 10000));
        int newRemediationId = nrmRemediationDao.insert(remediationToInsert);

        // ---[ Delete and verify ]---
        assertEquals(1, nrmRemediationDao.delete(newRemediationId));
    }
}