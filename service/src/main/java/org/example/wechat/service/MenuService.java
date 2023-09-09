package org.example.wechat.service;

import org.example.model.wechat.Menu;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.vo.wechat.MenuVo;

import java.util.List;

/**
 * <p>
 * 菜单 服务类
 * </p>
 *
 * @author lxc
 * @since 2023-09-08
 */
public interface MenuService extends IService<Menu> {

    List<MenuVo> findMenuInfo();

    void syncMenu();

    void removeMenu();
}
