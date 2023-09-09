package org.example.wechat.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.model.wechat.Menu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 菜单 Mapper 接口
 * </p>
 *
 * @author lxc
 * @since 2023-09-08
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

}
