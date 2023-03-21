package com.example.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.reggie.entity.Category;
import com.example.reggie.entity.Dish;
import com.example.reggie.entity.Setmeal;
import com.example.reggie.exception.CategoryBizException;
import com.example.reggie.exception.ExceptionDef;
import com.example.reggie.mapper.CategoryMapper;
import com.example.reggie.service.CategoryService;
import com.example.reggie.service.DishService;
import com.example.reggie.service.SetmealService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    @Override
    public boolean checkAndRemoveById(Long id) {
        // 1.删除Category前进行关联性检查
        // 1.1检查是否有关联菜品
        LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
        dishQueryWrapper.eq(Dish::getCategoryId, id);
        long dishCount = dishService.count(dishQueryWrapper);
        if (dishCount > 0) {
            throw new CategoryBizException(ExceptionDef.CATEGORY_DELETE_WITH_DISH_ERR);
        }

        // 1.2检查是否有相关联套餐
        LambdaQueryWrapper<Setmeal> setmealQueryWrapper = new LambdaQueryWrapper<>();
        setmealQueryWrapper.eq(Setmeal::getCategoryId, id);
        long setmealCount = setmealService.count(setmealQueryWrapper);
        if (setmealCount > 0) {
            throw new CategoryBizException(ExceptionDef.CATEGORY_DELETE_WITH_SETMEAL_ERR);
        }

        // 2.关联性检查通过，删除分类
        this.removeById(id);
        return true;
    }
}
