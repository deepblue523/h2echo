package com.deepblue.h2echo.test.dao.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.deepblue.h2echo.H2Echo;
import com.deepblue.h2echo.annotations.EchoDao;
import com.deepblue.h2echo.test.dao.beans.NrmCallGroup;
import com.deepblue.h2echo.test.utils.GeneralUtils;
import com.deepblue.h2echo.test.support.TestUtils;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

class NrmCallGroupsDaoTest {

    @EchoDao
    private NrmCallGroupsDao nrmCallGroupsDao;

    public NrmCallGroupsDaoTest() {
        H2Echo.echoDaosOnObject(this, true);
    }

    @Test
    void testGetById() {
        // Insert!
        NrmCallGroup callGroupPreInsert = TestUtils.getDummyNrmCallGroup();
        int newCallGroupId = nrmCallGroupsDao.insert(callGroupPreInsert);

        // Verify.
        NrmCallGroup callGroupPostInsert = nrmCallGroupsDao.getById(newCallGroupId);
        assertTrue(GeneralUtils.compareObjectsShallow(callGroupPostInsert, callGroupPreInsert,
                true, "id", "createdAt", "updatedAt"));
    }

    @Test
    void testInsert() {
        // Insert!
        NrmCallGroup callGroupPreInsert = new NrmCallGroup();

        callGroupPreInsert.setId(0);
        callGroupPreInsert.setCidrGroupId(100);
        callGroupPreInsert.setName("Sample Call Group");
        callGroupPreInsert.setDescription("This is a dummy call group for testing purposes");
        callGroupPreInsert.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        callGroupPreInsert.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        int newCallGroupId = nrmCallGroupsDao.insert(callGroupPreInsert);

        // ---[ Read back ]---
        NrmCallGroup callGroupPostInsert = nrmCallGroupsDao.getById(newCallGroupId);
        assertNotNull(callGroupPostInsert);

        // Verify.
        assertNotEquals(0, newCallGroupId);
        assertTrue(GeneralUtils.compareObjectsShallow(callGroupPostInsert, callGroupPreInsert,
                true, "id", "createdAt", "updatedAt"));
    }

    @Test
    void testUpdate() {
        // Insert!
        NrmCallGroup callGroupPreInsert = new NrmCallGroup();

        callGroupPreInsert.setId(0);
        callGroupPreInsert.setCidrGroupId(100);
        callGroupPreInsert.setName("Sample Call Group");
        callGroupPreInsert.setDescription("This is a dummy call group for testing purposes");
        callGroupPreInsert.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        callGroupPreInsert.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        int newCallGroupId = nrmCallGroupsDao.insert(callGroupPreInsert);

        // ---[ Read it back and modify ]---
        NrmCallGroup nrmCallGroupPostInsert = nrmCallGroupsDao.getById(newCallGroupId);
        assertNotNull(nrmCallGroupPostInsert);

        nrmCallGroupPostInsert.setCidrGroupId(200);
        nrmCallGroupPostInsert.setName("Sample Call Group (2)");
        nrmCallGroupPostInsert.setDescription("This is a dummy call group for testing purposes (2)");

        nrmCallGroupsDao.update(nrmCallGroupPostInsert);

        // ---[ Read it back again and compare to make sure update worked ]---
        NrmCallGroup nrmCallGroupReadback = nrmCallGroupsDao.getById(newCallGroupId);

        assertNotNull(nrmCallGroupPostInsert);
        assertTrue(GeneralUtils.compareObjectsShallow(
                        nrmCallGroupReadback, nrmCallGroupPostInsert,true, "updatedAt"));
    }

    @Test
    void testDelete() {
        // ---[ Insert! ]---
        NrmCallGroup callGroupInsert = new NrmCallGroup();

        callGroupInsert.setId(0);
        callGroupInsert.setCidrGroupId(100);
        callGroupInsert.setName("Sample Call Group");
        callGroupInsert.setDescription("This is a dummy call group for testing purposes");
        callGroupInsert.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        callGroupInsert.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        int newCallGroupId = nrmCallGroupsDao.insert(callGroupInsert);

        // ---[ Delete! ]---
        assertEquals(1, nrmCallGroupsDao.delete(newCallGroupId));
    }
}