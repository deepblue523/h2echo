package com.deepblue.h2echo.test.dao.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.deepblue.h2echo.H2Echo;
import com.deepblue.h2echo.annotations.EchoDao;
import com.deepblue.h2echo.test.dao.beans.NrmEntity;
import com.deepblue.h2echo.test.utils.GeneralUtils;
import org.junit.jupiter.api.Test;

class NrmEntityDaoTest {

    @EchoDao
    private NrmEntityDao nrmEntityDao;

    public NrmEntityDaoTest() {
        H2Echo.echoDaosOnObject(this, true);
    }

    @Test
    void testInsertAndReadback() {
        // ---[ Insert! ]---
        NrmEntity entityToInsert = new NrmEntity();
        entityToInsert.setId(0);
        entityToInsert.setEntityCode("code123");
        entityToInsert.setEntityName("name");
        entityToInsert.setRootEntityCode("rootCode");

        int newEntityId = nrmEntityDao.insert(entityToInsert);

        // ---[ Read back and compare ]---
        NrmEntity entityReadback = nrmEntityDao.getById(newEntityId);

        assertNotNull(entityReadback);
        assertTrue(GeneralUtils.compareObjectsShallow(entityReadback, entityToInsert, true,
                "id", "createdAt", "updatedAt"));
    }

    @Test
    public void testUpdate() {
        // ---[ Insert! ]---
        NrmEntity entityToInsert = new NrmEntity();
        entityToInsert.setId(0);
        entityToInsert.setEntityCode("code456");
        entityToInsert.setEntityName("name");
        entityToInsert.setRootEntityCode("rootCode");

        int newEntityId = nrmEntityDao.insert(entityToInsert);

        // ---[ Modify and store the record ]---
        NrmEntity entityToUpdate = nrmEntityDao.getById(newEntityId);

        entityToUpdate.setEntityCode("newCode789");
        entityToUpdate.setEntityName("newName");
        entityToUpdate.setRootEntityCode("newRootCode");

        nrmEntityDao.update(entityToUpdate);

        // ---[ Read back and verify the update ]---
        NrmEntity entityReadback = nrmEntityDao.getById(newEntityId);

        assertNotNull(entityReadback);
        assertTrue(GeneralUtils.compareObjectsShallow(entityReadback, entityToUpdate, true,
                "id", "createdAt", "updatedAt"));
    }

    @Test
    void testDelete() {
        // ---[ Insert ]---
        NrmEntity entityToInsert = new NrmEntity();
        entityToInsert.setId(0);
        entityToInsert.setEntityCode("code101010");
        entityToInsert.setEntityName("name");
        entityToInsert.setRootEntityCode("rootCode");

        int newEntityId = nrmEntityDao.insert(entityToInsert);

        // ---[ Delete and verify ]---
        assertEquals(1, nrmEntityDao.delete(newEntityId));
    }
}