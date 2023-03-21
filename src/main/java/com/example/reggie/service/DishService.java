package com.example.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.reggie.dto.DishDto;
import com.example.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    boolean saveWithFlavors(DishDto dishDto);

    DishDto getByIdWithFlavors(Long id);

    boolean updateWithFlavors(DishDto dishDto);
}
