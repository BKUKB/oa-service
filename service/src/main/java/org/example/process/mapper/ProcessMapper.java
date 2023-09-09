package org.example.process.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.model.process.Process;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.vo.process.ProcessQueryVo;
import org.example.vo.process.ProcessVo;

/**
 * <p>
 * 审批类型 Mapper 接口
 * </p>
 *
 * @author lxc
 * @since 2023-09-07
 */
@Mapper
public interface ProcessMapper extends BaseMapper<Process> {
    public IPage<ProcessVo> selectPage(Page<ProcessVo> processPage, @Param("vo") ProcessQueryVo processQueryVo);
}
