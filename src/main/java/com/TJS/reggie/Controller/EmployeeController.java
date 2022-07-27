package com.TJS.reggie.Controller;


import com.TJS.reggie.Service.EmployeeService;
import com.TJS.reggie.common.R;
import com.TJS.reggie.domain.Employee;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@RestController
@Slf4j
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //将密码加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //根据用户名查数据库
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Employee::getUsername,employee.getUsername());
        Employee employee1 = employeeService.getOne(lqw);
        //看看是否查到 以及密码是否正确
        if(employee1==null||!employee1.getPassword().equals(password)){
            return R.error("用户名或密码错误！");
        }
        //查看用户状态码是否激活
        if(employee1.getStatus()==0)return R.error("用户未激活");
        //登录成功 将Id存入session域
        request.getSession().setAttribute("employee",employee1.getId());

        return R.success(employee1);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("emoloyee");
        return R.success("退出成功！");
    }

    @PostMapping
    public R<Employee> save(@RequestBody Employee employee){
        /*
        id会采用雪花算法生成，见配置
         */
        //设置初始密码，并用md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //创建时间和更新时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        //设置创建人和更新人
//        Long id = (Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(id);
//        employee.setUpdateUser(id);
        //插入数据库表
        employeeService.save(employee);
        return R.success(employee);
    }

    @GetMapping("/page")
    public R<Page<Employee>> Getpage(int page,int pageSize,String name){
        //设置page
        Page<Employee> P = new Page<>(page,pageSize);

        //设置查询条件
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        lqw.like(name!=null,Employee::getName,name);
        lqw.orderByDesc(Employee::getUpdateTime);
        //查询数据库
        employeeService.page(P,lqw);
        return R.success(P);
    }
    @PutMapping
    public R<String> updateStatus(@RequestBody Employee employee){
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setUpdateUser(empId);
//        employee.setUpdateTime(LocalDateTime.now());
        //不用设置status，因为发生的status就是要修改成的样子。
        boolean update = employeeService.updateById(employee);
        log.info("是否更新成功:{}",update);

        return R.success("修改成功");
    }
    @GetMapping("/{id}")
    public R<Employee> GetEmployeeById(@PathVariable long id){
        Employee employee = employeeService.getById(id);
        if(employee==null)return R.error("没有查询到该员工信息");
        return R.success(employee);
    }
}
