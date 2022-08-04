package com.kiki.seckill.service;

import com.kiki.seckill.common.exception.GlobalException;
import com.kiki.seckill.common.result.CodeMsg;
import com.kiki.seckill.common.result.Result;
import com.kiki.seckill.common.utils.ValidatorUtil;
import com.kiki.seckill.dao.UserDao;
import com.kiki.seckill.pojo.SeckillUser;
import com.kiki.seckill.pojo.User;
import com.kiki.seckill.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;

@Service
public class UserService {
    @Autowired
    UserDao userDao;

    public User getById(int id) {
        return userDao.getById(id);
    }

    //使用事务
    @Transactional
    public boolean tx() {
        User user1=new User();
        user1.setId(3);
        user1.setName("kiki1");
        userDao.insert(user1);

        User user2=new User();
        user2.setId(1);
        user2.setName("kiki2");
        userDao.insert(user2);			//这里出问题则回滚
        return true;
    }






}

