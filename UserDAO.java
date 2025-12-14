package com.eventmgmt.dao;

import com.eventmgmt.model.User;

public interface UserDAO extends GenericDAO<User> {
    User getUserByEmail(String email);
}
