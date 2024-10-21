package com.deepblue.h2echo.test.dao;

import com.deepblue.h2echo.test.dao.beans.NrmRemediation;

public interface INrmRemediationDao extends INrmBaseDao<NrmRemediation> {
    int insert(NrmRemediation remediation);
    int update(NrmRemediation remediation);
    int delete(int id);
    NrmRemediation getById(int id);
}
