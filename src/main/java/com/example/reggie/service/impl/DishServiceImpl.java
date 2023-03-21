package com.example.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.reggie.dto.DishDto;
import com.example.reggie.entity.Category;
import com.example.reggie.entity.Dish;
import com.example.reggie.entity.DishFlavor;
import com.example.reggie.mapper.DishMapper;
import com.example.reggie.service.CategoryService;
import com.example.reggie.service.DishFlavorService;
import com.example.reggie.service.DishService;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Override
    @Transactional
    public boolean saveWithFlavors(DishDto dishDto) {
        // 1.dish表中插入新菜品
        this.save(dishDto);

        // 2.口味表中插入菜品对应的口味
        // 2.1为口味实体添加dish外键信息
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor :flavors) {
            flavor.setDishId(dishDto.getId());
        }
        // 2.2批量插入口味表
        dishFlavorService.saveBatch(flavors);

        return true;
    }

    @Override
    public DishDto getByIdWithFlavors(Long id) {
        // 1.根据id获取菜品
        Dish dish = this.getById(id);

        // 2.拷贝dish属性至dishDto
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        // 3.根据dishId获取口味列表
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> list = dishFlavorService.list(queryWrapper);

        // 4.设置口味列表
        dishDto.setFlavors(list);

        // 5.返还dishDto
        return dishDto;
    }

    @Override
    @Transactional
    public boolean updateWithFlavors(DishDto dishDto) {
        // 1.更新dish表
        this.updateById(dishDto);

        // 2.更新DishFlavor表
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishDto.getId());
        }

        dishFlavorService.updateBatchById(flavors);

        // 3.返回
        return true;
    }
}
