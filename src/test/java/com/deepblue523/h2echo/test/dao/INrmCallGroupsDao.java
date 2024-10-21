package com.deepblue523.h2echo.test.dao;

import com.deepblue523.h2echo.test.dao.beans.NrmCallGroup;

public interface INrmCallGroupsDao extends com.deepblue523.h2echo.test.dao.INrmBaseDao<NrmCallGroup> {
    NrmCallGroup getById(int id);
    int insert(NrmCallGroup callGroup);
    int update(NrmCallGroup callGroup);
    int delete(int id);
}