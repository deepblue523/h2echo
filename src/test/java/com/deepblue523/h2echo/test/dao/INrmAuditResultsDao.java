package com.deepblue523.h2echo.test.dao;

import com.deepblue523.h2echo.test.dao.beans.NrmAuditResult;

public interface INrmAuditResultsDao extends INrmBaseDao<NrmAuditResult> {
    int insert(NrmAuditResult auditResult);
    NrmAuditResult getById(int id);
    int update(NrmAuditResult auditResult);
    int delete(int id);
}
