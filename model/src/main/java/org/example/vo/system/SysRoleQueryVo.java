package org.example.vo.system;

import java.io.Serializable;

public class SysRoleQueryVo implements Serializable {
    private static final long serialVersionUID = 1L;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    private String roleName;


}
