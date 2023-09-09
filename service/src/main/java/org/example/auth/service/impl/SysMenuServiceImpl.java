package org.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.auth.mapper.SysMenuMapper;
import org.example.auth.service.SysMenuService;
import org.example.auth.service.SysRoleMenuService;
import org.example.auth.util.MenuHelper;
import org.example.common.config.exception.MyException;
import org.example.model.system.SysMenu;
import org.example.model.system.SysRoleMenu;
import org.example.vo.system.AssginMenuVo;
import org.example.vo.system.MetaVo;
import org.example.vo.system.RouterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author jerry
 * @since 2023-03-02
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    @Override
    public List<SysMenu> findNodes() {
        // 1、查询所有 的数据
        List<SysMenu> sysMenuList = baseMapper.selectList(null);

        // 2、构建树形结构
        List<SysMenu> list = MenuHelper.buildTree(sysMenuList);

        return list;
    }

    // 删除菜单
    @Override
    public void removeMenuById(Long id) {
        // 判断当前菜单是否有下一层菜单
        LambdaQueryWrapper<SysMenu> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysMenu::getParentId, id);

        Integer count = baseMapper.selectCount(lambdaQueryWrapper);

        if (count > 0) {
            throw new MyException(201, "菜单不能删除");
        }
        baseMapper.deleteById(id);
    }

    // 查询所有菜单和角色分配的菜单
    @Override
    public List<SysMenu> findMenuByRoleId(Long roleId) {
        // 1、查询所有的菜单 前提是 status=1
        LambdaQueryWrapper<SysMenu> sysMenuLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysMenuLambdaQueryWrapper.eq(SysMenu::getStatus, 1);
        List<SysMenu> sysMenuList = baseMapper.selectList(sysMenuLambdaQueryWrapper);

        // 2、根据 role_id 查询 sys_role_menu 对应的所有 menu_id
        LambdaQueryWrapper<SysRoleMenu> sysRoleMenuLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysRoleMenuLambdaQueryWrapper.eq(SysRoleMenu::getRoleId, roleId);
        List<SysRoleMenu> sysRoleMenuList = sysRoleMenuService.list(sysRoleMenuLambdaQueryWrapper);

        // 3、根据 menu_id ， 获取菜单对象
        List<Long> menuIdList = sysRoleMenuList.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());

        // 3.1、 拿着 menu_id 和所有的 菜单集合的 id 进行比较， 如果相同，就封装
        sysMenuList.forEach(item -> {
            if (menuIdList.contains(item.getId())) {
                item.setSelect(true);
            } else {
                item.setSelect(false);
            }
        });

        // 4、返回规定格式的菜单列表
        List<SysMenu> list = MenuHelper.buildTree(sysMenuList);

        return list;
    }

    // 为角色分配菜单
    @Override
    public void doAssign(AssginMenuVo assginMenuVo) {

        // 1、根据 role_id  删除 sys_role_menu 中的数据
        LambdaQueryWrapper<SysRoleMenu> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysRoleMenu::getRoleId, assginMenuVo.getRoleId());

        sysRoleMenuService.remove(lambdaQueryWrapper);

        // 2、从 assginMenuVo 获取新的 role_id 对应的 menu_id 集合
        List<Long> menuIdList = assginMenuVo.getMenuIdList();

        // 遍历集合，把 menu_id 和 role_id 重新设置上
        for (Long menuId : menuIdList) {
            if (StringUtils.isEmpty(menuId)) {
                continue;
            }
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setMenuId(menuId);
            sysRoleMenu.setRoleId(assginMenuVo.getRoleId());

            sysRoleMenuService.save(sysRoleMenu);
        }


    }

    // 根据 用户id 获取用户可以操作的菜单列表
    @Override
    public List<RouterVo> findUserMenuListByUserId(Long userId) {
        List<SysMenu> sysMenusList = null;
        // 1、判断当前用户是否是管理员       userId=1 是管理员
        // 1.1、 如果是管理员，查询所有菜单列表
        if (userId.longValue() == 1) {
            // 查询所有菜单列表
            LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysMenu::getStatus, 1);
            queryWrapper.orderByAsc(SysMenu::getSortValue);
            sysMenusList = baseMapper.selectList(queryWrapper);
        } else {
            // 1.2、如果不是管理员，根据 userId 查询可以操作菜单列表
            // 多表关联查询:sys_role、sys_role_mexnu、sys_menu
            sysMenusList = baseMapper.findMenuListByUserId(userId);
        }

        // 2、把查询出来的数据列表， 构建成框架要求的路由结构
        // 先构建树形结构
        List<SysMenu> sysMenuTreeList = MenuHelper.buildTree(sysMenusList);
        // 构建框架要求的路由结构
        List<RouterVo> routerList = this.buildRouter(sysMenuTreeList);

        return routerList;
    }

    // 构建框架要求的路由结构
    private List<RouterVo> buildRouter(List<SysMenu> menus) {
        // 创建 list 集合，存值最终数据
        List<RouterVo> routers = new ArrayList<>();
        // menus 遍历
        for (SysMenu menu : menus) {
            RouterVo router = new RouterVo();
            router.setHidden(false);
            router.setAlwaysShow(false);
            router.setPath(getRouterPath(menu));
            router.setComponent(menu.getComponent());
            router.setMeta(new MetaVo(menu.getName(), menu.getIcon()));
            // 下一层数据
            List<SysMenu> children = menu.getChildren();
            if (menu.getType().intValue() == 1) {
                // 加载隐藏路由
                List<SysMenu> hiddenMenuList = children.stream().filter(item -> !StringUtils.isEmpty(item.getComponent())).collect(Collectors.toList());
                for (SysMenu hiddenMenu : hiddenMenuList) {
                    RouterVo hiddenRouter = new RouterVo();
                    hiddenRouter.setHidden(true);
                    hiddenRouter.setAlwaysShow(false);
                    hiddenRouter.setPath(getRouterPath(hiddenMenu));
                    hiddenRouter.setComponent(hiddenMenu.getComponent());
                    hiddenRouter.setMeta(new MetaVo(hiddenMenu.getName(), hiddenMenu.getIcon()));
                    routers.add(hiddenRouter);
                }
            }else {
                if (!CollectionUtils.isEmpty(children)) {
                    if(children.size() > 0) {
                        router.setAlwaysShow(true);
                    }
                    // 递归
                    router.setChildren(buildRouter(children));
                }
            }
            routers.add(router);
        }
        return routers;

    }

    /**
     * 获取路由地址
     *
     * @param menu 菜单信息
     * @return 路由地址
     */
    public String getRouterPath(SysMenu menu) {
        String routerPath = "/" + menu.getPath();
        if (menu.getParentId().intValue() != 0) {
            routerPath = menu.getPath();
        }
        return routerPath;
    }

    // 根据 用户id 获取用户可以操作的按钮列表
    @Override
    public List<String> findUserPermsByUserId(Long userId) {
        // 1、判断是否是管理员，如果是管理员，查询所有按钮列表
        List<SysMenu> sysMenusList = null;
        if (userId.longValue() == 1) {
            // 查询所有菜单列表
            LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysMenu::getStatus, 1);
            sysMenusList = baseMapper.selectList(queryWrapper);
        }else {
            // 2、如果不是管理员，根据userId查询可以操作按钮列表
            // 多表关联查询:sys_role、sys_role_menu、sys_menu
            sysMenusList = baseMapper.findMenuListByUserId(userId);
        }

        // 3、从查询出来的数据里面，获取可以操作按钮值的List集合，返回
        List<String> permsList = sysMenusList.stream()
                .filter(item -> item.getType() == 2)
                .map(item -> item.getPerms())
                .collect(Collectors.toList());
        return permsList;
    }

    @Override
    public List<String> findUserPermsList(Long userId) {
        // 1、判断是否是管理员，如果是管理员，查询所有按钮列表
        List<SysMenu> sysMenusList = null;
        if (userId.longValue() == 1) {
            // 查询所有菜单列表
            LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysMenu::getStatus, 1);
            sysMenusList = baseMapper.selectList(queryWrapper);
        }else {
            // 2、如果不是管理员，根据userId查询可以操作按钮列表
            // 多表关联查询:sys_role、sys_role_menu、sys_menu
            sysMenusList = baseMapper.findMenuListByUserId(userId);
        }

        // 3、从查询出来的数据里面，获取可以操作按钮值的List集合，返回
        List<String> permsList = sysMenusList.stream()
                .filter(item -> item.getType() == 2)
                .map(item -> item.getPerms())
                .collect(Collectors.toList());
        return permsList;
    }
}
