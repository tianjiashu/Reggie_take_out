package com.TJS.reggie.Mapper;

import com.TJS.reggie.domain.Employee;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface EmployMapper extends BaseMapper<Employee> {
}
