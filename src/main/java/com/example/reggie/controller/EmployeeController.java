package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.R;
import com.example.reggie.common.util.BaseContext;
import com.example.reggie.entity.Employee;
import com.example.reggie.exception.ExceptionDef;
import com.example.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/employee")
@Slf4j
public class EmployeeController {
    private final String EMPLOYEE_ID= "employee_id";
    private final String INIT_PASSWORD = "123456";
    @Autowired
    EmployeeService employeeService;

    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        // 1.获取用户加密密码
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 2.获取数据库用户
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee one = employeeService.getOne(queryWrapper);

        // 3.比对用户密码
        if (one == null) {
            return R.error("用户名错误");
        }

        if (!password.equals(one.getPassword())) {
            return R.error("密码错误");
        }

        // 4.session域设置用户id
        request.getSession().setAttribute(EMPLOYEE_ID, one.getId());
        return R.success(one);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        // session域获移除用户id
        request.removeAttribute(EMPLOYEE_ID);
        return R.success("登出成功");
    }

    @PostMapping
    public R<String> create(HttpServletRequest servletRequest, @RequestBody Employee employee) {
        // 1.设置员工初始化密码为123456，并使用MD5加密
        employee.setPassword(
                DigestUtils.md5DigestAsHex(INIT_PASSWORD.getBytes()));

        // 2.设置员工的创建时间与更改时间（由公共字段填充器填充）

        // 3.设置员工的创建者（由公共字段填充器填充）
        Long createUserId = (Long) servletRequest.getSession().getAttribute(EMPLOYEE_ID);
        // 设置用户id上下文信息
        BaseContext.setSessionUserId(createUserId);

        // 4.数据库插入新员工
        employeeService.save(employee);

        // 5.返回成功
        return R.success("员工创建成功");
    }

    @GetMapping("/page")
    public R<Page<Employee>> pageQuery(
            @RequestParam Integer page,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String name) {

        log.info("员工分页查询，参数：page {}, pageSize {}, name {}", page, pageSize, name);

        // 1.始化页面信息
        Page<Employee> pageInfo = new Page<>(page, pageSize);

        // 2.配置查询条件
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);

        // 3.mapper查询
        employeeService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("更新员工信息：employee {}", employee);
        // 1.重新设置员工更新时间（由公共属性填充器填充）

        // 2.重新设置员工更新操作人员（由公共属性填充器填充）
        Long operateId = (Long) request.getSession().getAttribute(EMPLOYEE_ID);
        BaseContext.setSessionUserId(operateId);

        // 3.更新员工状态信息
        employeeService.updateById(employee);

        return R.success("更新员工信息成功");
    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable("id") Long id) {
        log.info("根据id查询员工信息：id {}", id);
        Employee employee = employeeService.getById(id);

        if (employee == null) {
            return R.error(ExceptionDef.EMPLOYEE_NOT_EXIST_ERR.getErrMsg());
        }

        return R.success(employee);
    }
}
