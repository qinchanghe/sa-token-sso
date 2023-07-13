package com.bd.mapper;

import com.bd.domain.SysUser;
import org.apache.ibatis.annotations.*;

/**
 * @ClassName SysUserMapper
 * @Author qch
 * @Description //TODO
 * @Date 2023/7/11 15:56
 * @Version 1.0
 **/
@Mapper
public interface SysUserMapper {
    @Select("select * from sys_user where username = #{username}")
    @Results(@Result(property = "userId", column = "user_id"))
    SysUser selectUser(@Param("username") String username);
}