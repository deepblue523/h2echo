package com.deepblue.h2echo.test.dao;

import com.deepblue.h2echo.test.dao.beans.NrmEntity;

public interface INrmEntityDao extends INrmBaseDao<NrmEntity> {
    int insert(NrmEntity entity);
    NrmEntity getById(int id);
    int update(NrmEntity entity);
    int delete(int id);
}
