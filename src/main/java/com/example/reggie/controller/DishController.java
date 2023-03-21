package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.R;
import com.example.reggie.dto.DishDto;
import com.example.reggie.entity.Category;
import com.example.reggie.entity.Dish;
import com.example.reggie.entity.DishFlavor;
import com.example.reggie.service.CategoryService;
import com.example.reggie.service.DishFlavorService;
import com.example.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    DishService dishService;
    @Autowired
    DishFlavorService dishFlavorService;
    @Autowired
    CategoryService categoryService;

    @PostMapping
    @CacheEvict(value = "dishCache", key = "#dishDto.categoryId")
    public R<String> create(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavors(dishDto);

        return R.success("创建新菜品成功");
    }


    @GetMapping("/page")
    public R<Page<DishDto>> page(
            @RequestParam Integer page,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String name) {

        // 1.构造分页信息
        Page<Dish> pageInfo = new Page(page, pageSize);

        // 2.构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = (new LambdaQueryWrapper<Dish>())
                .like(StringUtils.isNotEmpty(name), Dish::getName, name)
                .orderByAsc(Dish::getSort)
                .orderByDesc(Dish::getUpdateTime);

        // 3.获取dish表分页查询结果
        dishService.page(pageInfo, queryWrapper);

        // 4.将实体类与Dto类进行转换
        Page<DishDto> dtoPage = new Page<>();
        // 4.1将pageInfo的信息拷贝到dtoPage中，不过应当排除records项
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        // 4.2获取DishDto列表的拷贝
        List<DishDto> dishDtoList = pageInfo.getRecords().stream().map((dish -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish, dishDto);

            // 根据categoryId查询categoryName
            Category category = categoryService.getById(dishDto.getCategoryId());
            dishDto.setCategoryName(category.getName());

            return dishDto;
        })).collect(Collectors.toList());
        // 4.3设置pageDto的records项
        dtoPage.setRecords(dishDtoList);

        // 5.返还查询结果
        return R.success(dtoPage);
    }

    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable("id") Long id) {
        return R.success(dishService.getByIdWithFlavors(id));
    }

    @PutMapping
    @CacheEvict(value = "dishCache", key = "#dishDto.categoryId")
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavors(dishDto);

        return R.success("更改菜品信息成功");
    }

    @GetMapping("/list")
    @Cacheable(value = "dishCache", key = "#categoryId")
    public R<List<DishDto>> list(@RequestParam(required = false) Long categoryId) {
        log.info("查询菜品列表：categoryId {}", categoryId);
        // 1.构造查询条件包裹器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(categoryId != null, Dish::getCategoryId, categoryId)
                .orderByAsc(Dish::getSort)
                .orderByDesc(Dish::getUpdateTime);

        // 2. 查询菜品列表
        List<Dish> list = dishService.list(queryWrapper);

        // 3. 每个菜品查询对应的口味
        List<DishDto> dishDtos = list.stream().map((item -> {
            // 拷贝属性
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            // 查询口味
            LambdaQueryWrapper<DishFlavor> flavorQueryWrapper = new LambdaQueryWrapper<>();
            flavorQueryWrapper.eq(DishFlavor::getDishId, item.getId());
            List<DishFlavor> flavors = dishFlavorService.list(flavorQueryWrapper);

            // 设置口味
            dishDto.setFlavors(flavors);

            return dishDto;
        })).collect(Collectors.toList());

        // 3. 返还列表信息
        return R.success(dishDtos);
    }
}
