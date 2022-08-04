package com.kiki.seckill.dao;

import com.kiki.seckill.pojo.User;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

@Repository  //声明此类用于访问数据库
@Mapper  //添加该注释后，在编译之后会生成相应的接口实现类。
public interface UserDao {
    @Select("select * from t_user where id=#{id}")//@Param("id")进行引用
    public User getById(@Param("id") int id);
    @Insert("insert into t_user(id,name) values(#{id},#{name})")  //id为自增的，所以可以不用设置id
    public void insert(User user);
}
