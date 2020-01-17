package com.lifan.community.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class AalphaDaoMybatisImpl implements AlphaDao{
    @Override
    public String select() {
        return "MyBatis";
    }
}
