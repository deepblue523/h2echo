package com.deepblue523.h2echo.test.dao;

import com.deepblue523.h2echo.test.dao.beans.NrmPhoneNumber;

public interface INrmPhoneNumberDao extends INrmBaseDao<NrmPhoneNumber> {
    int insert(NrmPhoneNumber phoneNumber);
    NrmPhoneNumber getById(int id);
    int update(NrmPhoneNumber phoneNumber);
    int delete(int id);
}
