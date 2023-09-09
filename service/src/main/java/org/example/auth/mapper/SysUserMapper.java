package org.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.model.system.SysUser;
import org.springframework.stereotype.Repository;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
