package com.ding.demo.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ding.demo.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT COUNT(*) FROM users WHERE username = #{username}")
    int countByUsername(String username);

    @Select("SELECT COUNT(*) FROM users WHERE email = #{email}")
    int countByEmail(String email);

    @Update("UPDATE users SET username = #{username}, email = #{email}, phone = #{phone}, updated_at = NOW() WHERE id = #{id}")
    int updateUserInfo(User user);
}