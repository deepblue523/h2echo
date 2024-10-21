package com.deepblue.h2echo.test.dao.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.deepblue.h2echo.annotations.EchoDao;
import com.deepblue.h2echo.H2Echo;
import com.deepblue.h2echo.test.dao.beans.NrmCallGroup;
import com.deepblue.h2echo.test.dao.beans.NrmPhoneNumber;
import com.deepblue.h2echo.test.utils.GeneralUtils;
import com.deepblue.h2echo.testsupport.TestUtils;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

class NrmPhoneNumberDaoTest {

    @EchoDao
    private NrmPhoneNumberDao nrmPhoneNumberDao;

    @EchoDao
    private NrmCallGroupsDao nrmCallGroupsDao;

    public NrmPhoneNumberDaoTest() {
        H2Echo.echoDaosOnObject(this, true);
    }

    @Test
    void testInsertAndReadback() {
        // ---[ Insert ]---
        NrmCallGroup callGroupPreInsert = TestUtils.getDummyNrmCallGroup();
        int newCallGroupId = nrmCallGroupsDao.insert(callGroupPreInsert);

        NrmPhoneNumber phoneNumberToInsert = new NrmPhoneNumber();
        phoneNumberToInsert.setId(0);
        phoneNumberToInsert.setCallGroupId(newCallGroupId);
        phoneNumberToInsert.setCidrId(123);
        phoneNumberToInsert.setPhoneNumber("1234567890");
        phoneNumberToInsert.setDescription("Test Description");
        phoneNumberToInsert.setDepartment("Test Department");
        phoneNumberToInsert.setInternalId("INT123");
        phoneNumberToInsert.setCnam("CNAM");
        phoneNumberToInsert.setUserId(789);
        phoneNumberToInsert.setNumeracleProfileId("Profile123");
        phoneNumberToInsert.setArchive(false);
        phoneNumberToInsert.setArchiveDate(new Timestamp(System.currentTimeMillis()));

        int newPhoneNumberId = nrmPhoneNumberDao.insert(phoneNumberToInsert);

        // ---[ Read back and verify ]---
        NrmPhoneNumber phoneNumberReadback = nrmPhoneNumberDao.getById(newPhoneNumberId);

        assertNotNull(phoneNumberReadback);
        assertTrue(GeneralUtils.compareObjectsShallow(
                phoneNumberReadback, phoneNumberToInsert, true, "id", "createdAt", "updatedAt"));
    }

    @Test
    void testUpdateById() {
        // ---[ Insert ]---
        NrmCallGroup callGroupPreInsert = TestUtils.getDummyNrmCallGroup();
        int newCallGroupId = nrmCallGroupsDao.insert(callGroupPreInsert);

        NrmPhoneNumber phoneNumberToInsert = new NrmPhoneNumber();
        phoneNumberToInsert.setId(0);
        phoneNumberToInsert.setCallGroupId(newCallGroupId);
        phoneNumberToInsert.setCidrId(123);
        phoneNumberToInsert.setPhoneNumber("1234567890");
        phoneNumberToInsert.setDescription("Test Description");
        phoneNumberToInsert.setDepartment("Test Department");
        phoneNumberToInsert.setInternalId("INT123");
        phoneNumberToInsert.setCnam("CNAM");
        phoneNumberToInsert.setUserId(789);
        phoneNumberToInsert.setNumeracleProfileId("Profile123");
        phoneNumberToInsert.setArchive(false);
        phoneNumberToInsert.setArchiveDate(new Timestamp(System.currentTimeMillis()));

        int newPhoneNumberId = nrmPhoneNumberDao.insert(phoneNumberToInsert);

        // ---[ Read back and modify the record ]---
        NrmPhoneNumber phoneNumberToUpdate = nrmPhoneNumberDao.getById(newPhoneNumberId);
        phoneNumberToUpdate.setCallGroupId(newCallGroupId);
        phoneNumberToUpdate.setCidrId(10101);
        phoneNumberToUpdate.setPhoneNumber("1111111111");
        phoneNumberToUpdate.setDescription("Test Description (2)");
        phoneNumberToUpdate.setDepartment("Test Department (2)");
        phoneNumberToUpdate.setInternalId("INT456");
        phoneNumberToUpdate.setCnam("CNAM-test");
        phoneNumberToUpdate.setUserId(2020202);
        phoneNumberToUpdate.setNumeracleProfileId("Profile789");
        phoneNumberToUpdate.setArchive(false);
        phoneNumberToUpdate.setArchiveDate(new Timestamp(System.currentTimeMillis()));
        nrmPhoneNumberDao.update(phoneNumberToUpdate);

        // ---[ Read back and verify ]---
        NrmPhoneNumber phoneNumberReadback = nrmPhoneNumberDao.getById(newPhoneNumberId);

        assertNotNull(phoneNumberReadback);
        assertTrue(GeneralUtils.compareObjectsShallow(
                phoneNumberReadback, phoneNumberToUpdate, true, "updatedAt"));
    }

    @Test
    void testDeleteById() {
        // ---[ Insert ]---
        NrmCallGroup callGroupPreInsert = TestUtils.getDummyNrmCallGroup();
        int newCallGroupId = nrmCallGroupsDao.insert(callGroupPreInsert);

        NrmPhoneNumber phoneNumberToInsert = new NrmPhoneNumber();
        phoneNumberToInsert.setId(0);
        phoneNumberToInsert.setCallGroupId(newCallGroupId);
        phoneNumberToInsert.setCidrId(123);
        phoneNumberToInsert.setPhoneNumber("222222222222");
        phoneNumberToInsert.setDescription("Test Description");
        phoneNumberToInsert.setDepartment("Test Department");
        phoneNumberToInsert.setInternalId("INT123");
        phoneNumberToInsert.setCnam("CNAM");
        phoneNumberToInsert.setUserId(789);
        phoneNumberToInsert.setNumeracleProfileId("Profile123");
        phoneNumberToInsert.setArchive(false);
        phoneNumberToInsert.setArchiveDate(new Timestamp(System.currentTimeMillis()));

        int newPhoneNumberId = nrmPhoneNumberDao.insert(phoneNumberToInsert);

        // ---[ Delete it ]---
        assertEquals(1, nrmPhoneNumberDao.delete(newPhoneNumberId));
    }
}