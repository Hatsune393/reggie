package com.example.reggie.dto;

import com.example.reggie.entity.Dish;
import com.example.reggie.entity.DishFlavor;
import lombok.Data;

import java.util.List;

@Data
public class DishDto extends Dish {
    private List<DishFlavor> flavors;
    private String categoryName;
}
