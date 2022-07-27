package com.TJS.reggie.Service.Impl;

import com.TJS.reggie.Mapper.EmployMapper;
import com.TJS.reggie.Service.EmployeeService;
import com.TJS.reggie.domain.Employee;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployMapper, Employee> implements EmployeeService {
}
