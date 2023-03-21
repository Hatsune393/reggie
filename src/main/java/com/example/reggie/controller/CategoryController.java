package com.example.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.R;
import com.example.reggie.common.util.BaseContext;
import com.example.reggie.entity.Category;
import com.example.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Autowired
    CategoryService categoryService;

    @GetMapping("/page")
    public R<Page<Category>> pageQuery(@RequestParam Integer page, @RequestParam Integer pageSize) {
        log.info("分类分页查询，查询参数：page {}, pageSize {}", page, pageSize);

        Page<Category> pageInfo = new Page<>(page, pageSize);

        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.orderByAsc(Category::getSort);

        return R.success(categoryService.page(pageInfo, queryWrapper));
    }

    @PostMapping
    public R<String> create(HttpServletRequest request, @RequestBody Category category) {
        log.info("新增分类，参数：category {}", category);

        Long employeeId = (Long) request.getSession().getAttribute("employee_id");
        BaseContext.setSessionUserId(employeeId);
        categoryService.save(category);

        return R.success("添加新分类成功");
    }

    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Category category) {
        log.info("更改分类，参数：category {}", category);

        Long employeeId = (Long) request.getSession().getAttribute("employee_id");
        BaseContext.setSessionUserId(employeeId);
        categoryService.updateById(category);

        return R.success("更改分类成功");
    }

    @DeleteMapping
    public R<String> delete(@RequestParam("ids") Long id) {
        log.info("删除分类，参数：id {}", id);

        categoryService.checkAndRemoveById(id);
        return R.success("删除分类成功");
    }

    @GetMapping("/list")
    public R<List<Category>> list(@RequestParam(value = "type", required = false) Integer type) {
        // 1.构造列表排序条件，按照sort字段升序，随后按照updateTime降序
        LambdaQueryWrapper<Category> queryWrapper =
                new LambdaQueryWrapper<Category>()
                        .eq(type!=null, Category::getType, type)
                        .orderByAsc(Category::getSort)
                        .orderByDesc(Category::getUpdateTime);

        // 2.返还列表结果
        return R.success(categoryService.list(queryWrapper));
    }

}
