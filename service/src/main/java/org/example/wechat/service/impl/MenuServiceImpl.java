package org.example.wechat.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import lombok.SneakyThrows;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.example.model.wechat.Menu;
import org.example.vo.wechat.MenuVo;
import org.example.wechat.mapper.MenuMapper;
import org.example.wechat.service.MenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单 服务实现类
 * </p>
 *
 * @author lxc
 * @since 2023-09-08
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {


    @Override
    public List<MenuVo> findMenuInfo() {
        //1 查询所有菜单list集合
        List<MenuVo> firstMenuVos = new ArrayList<>();
        List<Menu> menus = baseMapper.selectList(null);
        //2 查询所有一级菜单 parent_id=0，返回一级菜单list集合
        List<Menu> firstMenus = menus.stream()
                .filter(menu -> menu.getParentId().longValue() == 0)
                .collect(Collectors.toList());
        //3 一级菜单list集合遍历，得到每个一级菜单
        for (Menu firstMenu : firstMenus) {
            //一级菜单Menu --- MenuVo
            MenuVo firstMenuVo = new MenuVo();
            BeanUtils.copyProperties(firstMenu, firstMenuVo);
            //4 获取每个一级菜单里面所有二级菜单 id 和 parent_id比较
            //一级菜单id  和  其他菜单parent_i
            // d
            //获得二级目录
            List<Menu> scondMenus = menus.stream()
                    .filter(menu -> menu.getParentId().longValue() == firstMenu.getId())
                    .sorted(Comparator.comparing(Menu::getSort))
                    .collect(Collectors.toList());
            //5 把一级菜单里面所有二级菜单获取到，封装一级菜单children集合里面
            //List<Menu> -- List<MenuVo>
            ArrayList<MenuVo> children = new ArrayList<>();
            for(Menu secondMenu : scondMenus){
                MenuVo secondMenuVo = new MenuVo();
                BeanUtils.copyProperties(secondMenu, secondMenuVo);
                children.add(secondMenuVo);
            }
            firstMenuVo.setChildren(children);
            //把每个封装好的一级菜单放到最终list集合
            firstMenuVos.add(firstMenuVo);
        }
        return firstMenuVos;
    }

    @Autowired
    private WxMpService wxMpService;

    @Override
    public void syncMenu() {
        List<MenuVo> menuVoList = this.findMenuInfo();
        //菜单
        JSONArray buttonList = new JSONArray();
        for(MenuVo oneMenuVo : menuVoList) {
            JSONObject one = new JSONObject();
            one.put("name", oneMenuVo.getName());
            if(CollectionUtils.isEmpty(oneMenuVo.getChildren())) {
                one.put("type", oneMenuVo.getType());
                one.put("url", "http://front.v5.idcfengye.com/#"+oneMenuVo.getUrl());
            } else {
                JSONArray subButton = new JSONArray();
                for(MenuVo twoMenuVo : oneMenuVo.getChildren()) {
                    JSONObject view = new JSONObject();
                    view.put("type", twoMenuVo.getType());
                    if(twoMenuVo.getType().equals("view")) {
                        view.put("name", twoMenuVo.getName());
                        //H5页面地址
                        view.put("url", "http://front.v5.idcfengye.com#"+twoMenuVo.getUrl());
                    } else {
                        view.put("name", twoMenuVo.getName());
                        view.put("key", twoMenuVo.getMeunKey());
                    }
                    subButton.add(view);
                }
                one.put("sub_button", subButton);
            }
            buttonList.add(one);
        }
        //菜单
        JSONObject button = new JSONObject();
        button.put("button", buttonList);
        try {
            wxMpService.getMenuService().menuCreate(button.toJSONString());
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    @Override
    public void removeMenu() {
        wxMpService.getMenuService().menuDelete();
    }
}
