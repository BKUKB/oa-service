package org.example.process.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.model.process.ProcessType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 审批类型 Mapper 接口
 * </p>
 *
 * @author lxc
 * @since 2023-09-07
 */
@Mapper
public interface Process_typeMapper extends BaseMapper<ProcessType> {

}
