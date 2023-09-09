package org.example.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.example.auth.service.SysRoleService;
import org.example.common.result.Result;
import org.example.model.system.SysRole;
import org.example.vo.system.AssginRoleVo;
import org.example.vo.system.SysRoleQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "角色管理接口")
@RestController
@RequestMapping("/admin/system/sysRole")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    // 1、查询所有角色 和 当前用户所属角色
    @ApiOperation("根据用户获取角色数据")
    @GetMapping("/toAssign/{userId}")
    public Result toAssign(@PathVariable Long userId) {
        Map<String, Object> map = sysRoleService.findRoleDataByUserId(userId);
        return Result.ok(map);
    }

    // 2、为用户分配角色
    @ApiOperation("为用户分配角色")
    @PostMapping("/doAssign")
    public Result doAssign(@RequestBody AssginRoleVo assginRoleVo) {
        sysRoleService.doAssign(assginRoleVo);
        return Result.ok();
    }

    /**
     * 查询所有角色, 统一返回数据结果
     *
     * @return
     */
    @ApiOperation("查询所有角色")
    @GetMapping("/getAll")
    public Result getAll() {
        List<SysRole> list = sysRoleService.list();
        return Result.ok(list);
    }

    /**
     * 条件分页查询
     *
     * @param page           当前页
     * @param pageSize       分页大小
     * @param sysRoleQueryVo 条件查询对象
     * @return
     */

    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation("条件分页查询")
    @GetMapping("{page}/{pageSize}")
    public Result page(@PathVariable int page, @PathVariable int pageSize, SysRoleQueryVo sysRoleQueryVo) {

        // 1、创建 page 对象， 传递分页查询的参数
        Page<SysRole> sysRolePage = new Page<>(page, pageSize);

        // 2、构造分页查询条件, 判断条件是否为空，不为空进行封装
        LambdaQueryWrapper<SysRole> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        String roleName = sysRoleQueryVo.getRoleName();
        if (!StringUtils.isEmpty(roleName)) {
            // 封装
            lambdaQueryWrapper.like(SysRole::getRoleName, roleName);
        }

        // 3、调用方法实现分页查询
        sysRoleService.page(sysRolePage, lambdaQueryWrapper);
        return Result.ok(sysRolePage);
    }

    /**
     * 添加角色
     *
     * @param sysRole
     * @return
     */

    @ApiOperation("添加角色")
    @PostMapping("/save")
    public Result save(@RequestBody SysRole sysRole) {
        // 调用 service 方法
        boolean is_success = sysRoleService.save(sysRole);
        if (is_success) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    /**
     * 根据 id 查询角色
     *
     * @param id
     * @return
     */

    @ApiOperation("根据 id 查询角色")
    @GetMapping("/get/{id}")
    public Result get(@PathVariable long id) {
        SysRole sysRole = sysRoleService.getById(id);
        return Result.ok(sysRole);
    }

    /**
     * 修改角色
     *
     * @param sysRole
     * @return
     */

    @ApiOperation("修改角色")
    @PutMapping("/update")
    public Result update(@RequestBody SysRole sysRole) {
        // 调用 service 方法
        boolean is_success = sysRoleService.updateById(sysRole);
        if (is_success) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    /**
     * 根据 id 删除
     *
     * @param id
     * @return
     */

    @ApiOperation("根据 id 删除")
    @DeleteMapping("delete/{id}")
    public Result deleteById(@PathVariable long id) {
        boolean is_success = sysRoleService.removeById(id);

        if (is_success) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    /**
     * 批量删除
     * 说明：
     * Java 中的对象会转化为Json对象
     * Java 中的List集合会转化为数组
     *
     * @param ids
     * @return
     */

    @ApiOperation("批量删除")
    @DeleteMapping("/batchRemove")
    public Result deleteByIds(@RequestBody List<Long> ids) {

        boolean is_success = sysRoleService.removeByIds(ids);

        if (is_success) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }
}