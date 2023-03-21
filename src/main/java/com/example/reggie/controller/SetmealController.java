package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.R;
import com.example.reggie.dto.SetmealDto;
import com.example.reggie.entity.Category;
import com.example.reggie.entity.Setmeal;
import com.example.reggie.entity.SetmealDish;
import com.example.reggie.service.CategoryService;
import com.example.reggie.service.SetmealDishService;
import com.example.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    SetmealService setmealService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    SetmealDishService setmealDishService;

    /**
     * 获取套餐分页列表
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<SetmealDto>> page(
            @RequestParam int page,
            @RequestParam int pageSize,
            @RequestParam(required = false) String name) {

        // 1.构造分页信息
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);

        // 2.构造套餐查询条件
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotEmpty(name), Setmeal::getName, name);

        // 3.查询套餐分页列表
        setmealService.page(pageInfo, queryWrapper);

        // 4.构造套餐Dto分页
        Page<SetmealDto> dtoPage = new Page<>();
        // 4.1拷贝页面信息给dtoPage
        BeanUtils.copyProperties(page, dtoPage, "records");
        // 4.2将套餐list转换为dto套餐list
        List<SetmealDto> setmealDtoList = new ArrayList<>();
        setmealDtoList = pageInfo.getRecords().stream().map(item -> {
            SetmealDto dto = new SetmealDto();
            BeanUtils.copyProperties(item, dto);
            // 获取分类名称并赋值给dto
            Category category = categoryService.getById(item.getCategoryId());
            dto.setCategoryName(category.getName());
            return dto;
        }).collect(Collectors.toList());

        // 4.3将dtoList注入给records
        dtoPage.setRecords(setmealDtoList);

        // 5.返还查询结果
        return R.success(dtoPage);
    }

    @PostMapping
    @CacheEvict(value = "setmealCache", key = "#setmealDto.categoryId")
    public R<String> create(@RequestBody SetmealDto setmealDto) {
        setmealService.saveWithDishes(setmealDto);

        return R.success("创建新套餐成功");
    }

    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable("id") Long id) {
        // 1.构造查询条件包裹器
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        Setmeal setmeal = setmealService.getById(id);

        // 2.将setmeal拷贝到setmealDto上
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);

        // 3.获取相关联菜品
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishList = setmealDishService.list(setmealDishLambdaQueryWrapper);
        setmealDto.setSetmealDishes(setmealDishList);

        // 4.获取套餐对应分类
        Category category = categoryService.getById(setmeal.getCategoryId());
        setmealDto.setCategoryName(category.getName());

        // 5.返回Dto
        return R.success(setmealDto);
    }

    @PutMapping
    @CacheEvict(value = "setmealCache", key = "#setmealDto.categoryId")
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        setmealService.updateWithDishes(setmealDto);

        return R.success("更新套餐信息成功");
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache", allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids) {
        setmealService.removeWithDishes(ids);

        return R.success("删除所有套餐成功");
    }

    /**
     * 批量启售/停售
     * @return
     */
    @PostMapping("/status/{status}")
    @CacheEvict(value = "setmealCache", allEntries = true)
    public R<String> changeStatus(@PathVariable("status") int status, @RequestParam List<Long> ids) {
        List<Setmeal> setmeals= ids.stream().map((id -> {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(id);
            setmeal.setStatus(status);
            return setmeal;
        })).collect(Collectors.toList());

        setmealService.updateBatchById(setmeals);

        return R.success("批量修改套餐状态成功");
    }

    @GetMapping("/list")
    @Cacheable(value = "setmealCache", key = "#categoryId")
    public R<List<Setmeal>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {

        // 1.构造查询条件器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(categoryId!=null, Setmeal::getCategoryId, categoryId)
                .eq(status!=null, Setmeal::getStatus, status);

        // 2.查询
        List<Setmeal> list = setmealService.list(queryWrapper);

        // 3.返还结果
        return R.success(list);
    }
}
