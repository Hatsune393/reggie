package com.example.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.reggie.dto.SetmealDto;
import com.example.reggie.entity.Setmeal;
import com.example.reggie.entity.SetmealDish;
import com.example.reggie.exception.ExceptionDef;
import com.example.reggie.exception.SetmealException;
import com.example.reggie.mapper.SetmealMapper;
import com.example.reggie.service.SetmealDishService;
import com.example.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    SetmealDishService setmealDishService;

    @Override
    @Transactional
    public boolean saveWithDishes(SetmealDto setmealDto) {
        // 1.存储套餐到套餐表
        this.save(setmealDto);

        // 2.生成套餐菜品关联列表
        for (SetmealDish setmealDish : setmealDto.getSetmealDishes()) {
            setmealDish.setSetmealId(setmealDto.getId());
        }

        // 3.将套餐菜品关系批量存储到菜品关系表
        setmealDishService.saveBatch(setmealDto.getSetmealDishes());

        return true;
    }

    @Override
    @Transactional
    public boolean updateWithDishes(SetmealDto setmealDto) {
        // 1.更新套餐到套餐表
        this.updateById(setmealDto);

        // 2.更新套餐菜品关联列表
        for (SetmealDish setmealDish : setmealDto.getSetmealDishes()) {
            setmealDish.setSetmealId(setmealDto.getId());
        }

        // 3.将套餐菜品关系批量更新到菜品关系表
        setmealDishService.updateBatchById(setmealDto.getSetmealDishes());

        return true;
    }

    @Override
    @Transactional
    public boolean removeWithDishes(List<Long> ids) {
        // 1.删除套餐相关的套餐餐品关系数据
        // delete from setmeal_dish where setmeal_id in (ids)
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(queryWrapper);

        // 2.删除套餐前需要检查所有套餐是否都处于停售状态
        // select count(*) from setmeal where id in (ids) and status = 1
        LambdaQueryWrapper<Setmeal> checkQueryWrapper = new LambdaQueryWrapper<>();
        checkQueryWrapper.in(Setmeal::getId, ids);
        checkQueryWrapper.eq(Setmeal::getStatus, 1);
        long count = this.count(checkQueryWrapper);
        if (count > 0) {
            throw new SetmealException(ExceptionDef.DELETE_LIVING_SETMEAL_ERR);
        }

        // 3.删除所有套餐
        // delete from setmeal where id in (ids)
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(Setmeal::getId, ids);
        this.remove(setmealLambdaQueryWrapper);

        // 3.返回
        return true;
    }
}
