package com.example.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.reggie.dto.SetmealDto;
import com.example.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    /**
     * 存储套餐信息到套餐表，并且存储套餐相关联的菜品到关系表
     * @param setmealDto
     * @return
     */
    boolean saveWithDishes(SetmealDto setmealDto);

    /**
     * 更新套餐信息
     * @param setmealDto
     * @return
     */
    boolean updateWithDishes(SetmealDto setmealDto);

    /**
     * 删除套餐信息，删除前需要将套餐餐品关系表也一并删除
     * @param ids
     * @return
     */
    boolean removeWithDishes(List<Long> ids);
}
