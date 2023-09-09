package org.example.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import org.example.auth.service.SysMenuService;
import org.example.auth.service.SysUserService;
import org.example.common.config.exception.MyException;
import org.example.common.jwt.JwtHelper;
import org.example.common.result.Result;
import org.example.common.util.MD5;
import org.example.model.system.SysUser;
import org.example.vo.system.LoginVo;
import org.example.vo.system.RouterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "后台登录接口")
@RestController
@RequestMapping("/admin/system/index")
public class IndexController{

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysMenuService sysMenuService;
    /**
     * 登录
     * @return
     */
    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("token","admin");
//        return Result.ok(map);
        // 1、获取用户名和密码，url
        // 2、根据用户名查询数据库
        String username = loginVo.getUsername();
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, username);
        SysUser sysUser = sysUserService.getOne(queryWrapper);

        // 3、用户信息是否存在
        if (sysUser == null) {
            throw new MyException(201, "用户不存在...");
        }

        // 4、判断密码
        // 取出数据库中的密文密码（MD5）
        String password_dB = sysUser.getPassword();
        String password_input = MD5.encrypt(loginVo.getPassword());

        if (!password_dB.equals(password_input)) {
            throw new MyException(201, "密码错误...");
        }

        // 5、判断用户是否被禁用  1  可用    0   禁用
        if (sysUser.getStatus().intValue() == 0) {
            throw new MyException(201, "用户被禁用...");
        }

        // 6、使用jwt根据用户id和用户名称生成token的字符串
        String token = JwtHelper.createToken(sysUser.getId(), sysUser.getUsername());

        // 7、返回
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        return Result.ok(map);
    }
    /**
     * 获取用户信息
     * @return
     */
    @GetMapping("/info")
    public Result info(HttpServletRequest request) {

        // 1、从请求头获取用户信息（获取请求头的 token 字符串）
        String token = request.getHeader("token");

        // 2、从 token 字符串中获取 用户id 或者 用户名称
        Long userId = JwtHelper.getUserId(token);

        // 3、根据 用户id 查询数据库， 获取用户信息
        SysUser sysUser = sysUserService.getById(userId);

        // 4、根据 用户id 获取用户可以操作的菜单列表
        // 查询数据库动态构建路由结构，进行显示
        List<RouterVo> routerList = sysMenuService.findUserMenuListByUserId(userId);

        // 5、根据 用户id 获取用户可以操作的按钮列表
        List<String> permsList = sysMenuService.findUserPermsByUserId(userId);

        // 6、返回相应的数据
        Map<String, Object> map = new HashMap<>();
        map.put("roles", "[admin]");
        map.put("name", sysUser.getName());
        map.put("avatar", "https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");

        // 返回用户可以操作的菜单
        map.put("routers", routerList);

        // 返回用户可以操作的按钮
        map.put("buttons", permsList);

        return Result.ok(map);
    }
    /**
     * 退出
     * @return
     */
    @PostMapping("logout")
    public Result logout(){
        return Result.ok();
    }
}
