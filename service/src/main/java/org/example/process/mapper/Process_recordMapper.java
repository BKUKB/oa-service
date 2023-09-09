package org.example.process.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.model.process.ProcessRecord;

/**
 * <p>
 * 审批记录 Mapper 接口
 * </p>
 *
 * @author lxc
 * @since 2023-09-07
 */
@Mapper
public interface Process_recordMapper extends BaseMapper<ProcessRecord> {

}
