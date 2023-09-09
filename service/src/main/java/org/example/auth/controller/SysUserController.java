package org.example.auth.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.example.auth.service.SysUserService;
import org.example.common.result.Result;
import org.example.common.util.MD5;
import org.example.model.system.SysUser;
import org.example.vo.system.SysUserQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.beans.Transient;
import java.util.List;

@Api(tags = "用户管理接口")
@RestController
@RequestMapping("/admin/system/sysUser")
@CrossOrigin //解决跨域问题
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @ApiOperation(value = "获取当前用户基本信息")
    @GetMapping("getCurrentUser")
    public Result getCurrentUser() {
        return Result.ok(sysUserService.getCurrentUser());
    }

    @ApiOperation(value = "获得全部用户信息")
    @GetMapping("/findAll")
    public Result<List<SysUser>> findAll(){
        List<SysUser> list = sysUserService.list();
        return Result.ok(list);
    }

    @ApiOperation(value = "更新状态")
    @GetMapping("/updateStatus/{id}/{status}")
    public Result updateStatus(@PathVariable Long id, @PathVariable Integer status){
        sysUserService.updateStatus(id, status);
        return Result.ok();
    }

    @ApiOperation("用户条件分页查询")
    @GetMapping("/{page}/{pageSize}")
    public Result page(@PathVariable int page, @PathVariable int pageSize, SysUserQueryVo sysUserQueryVo) {
        Page<SysUser> sysUserPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<SysUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 获取条件
        String userName = sysUserQueryVo.getKeyword();
        String createTimeBegin = sysUserQueryVo.getCreateTimeBegin();
        String createTimeEnd = sysUserQueryVo.getCreateTimeEnd();

        // 判断条件值不为空
        if (!StringUtils.isEmpty(userName)){
            lambdaQueryWrapper.like(SysUser::getUsername,userName);
        }
        if (!StringUtils.isEmpty(createTimeBegin)){
            lambdaQueryWrapper.ge(SysUser::getCreateTime,createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)){
            lambdaQueryWrapper.le(SysUser::getCreateTime,createTimeEnd);
        }

        sysUserService.page(sysUserPage,lambdaQueryWrapper);

        return Result.ok(sysUserPage);
    }

    @ApiOperation("获取用户")
    @GetMapping("/get/{id}")
    public Result get(@PathVariable long id){
        SysUser user = sysUserService.getById(id);
        return Result.ok(user);
    }

    @ApiOperation("更新用户")
    @PutMapping("/update")
    public Result update(@RequestBody SysUser sysUser){
        boolean is_success = sysUserService.updateById(sysUser);
        if (is_success) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    @ApiOperation("保存用户")
    @PostMapping("/save")
    public Result save(@RequestBody SysUser sysUser){

        // 密码要进行加密处理， MD5加密
        String pwdMD5 = MD5.encrypt(sysUser.getPassword());
        sysUser.setPassword(pwdMD5);

        boolean is_success = sysUserService.save(sysUser);
        if (is_success) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/remove/{id}")
    public Result remove(@PathVariable long id){
        boolean is_success = sysUserService.removeById(id);
        if (is_success) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    @ApiOperation("批量删除")
    @DeleteMapping("/batchRemove")
    public Result deleteByIds(@RequestBody List<Long> ids) {

        boolean is_success = sysUserService.removeByIds(ids);

        if (is_success) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }
}
