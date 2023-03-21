package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.reggie.common.R;
import com.example.reggie.common.util.BaseContext;
import com.example.reggie.entity.ShoppingCart;
import com.example.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    ShoppingCartService shoppingCartService;

    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        // 1.获取当前用户id
        Long userId = BaseContext.getSessionUserId();

        // 2.构造查询条件器
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        // 3.查询当前用户所有购物车数据
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        // 4.返还结果
        return R.success(list);
    }

    @PostMapping("/add")
    public R<String> create(@RequestBody ShoppingCart shoppingCart) {
        // 1.获取当前用户id
        Long userId = BaseContext.getSessionUserId();

        // 2.设置shoppingCart用户id信息
        shoppingCart.setUserId(userId);

        // 3.查询当前购物车是否已经含有该 菜品/套餐 数据，若已有则数量加一
        if (shoppingCart.getDishId() != null) {
            // 3.1当前购物车数据包含的是菜品数据
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShoppingCart::getUserId, userId);
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());

            ShoppingCart one = shoppingCartService.getOne(queryWrapper);
            if (one != null) {
                one.setNumber(one.getNumber() + 1);
                shoppingCartService.updateById(one);
                return R.success("添加购物车信息成功");
            }
        }

        if (shoppingCart.getSetmealId() != null) {
            // 3.2当前购物车数据包含的是套餐数据
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShoppingCart::getUserId, userId);
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());

            ShoppingCart one = shoppingCartService.getOne(queryWrapper);
            shoppingCart.setNumber(one == null ? 1 : one.getNumber() + 1);

            if (one != null) {
                one.setNumber(one.getNumber() + 1);
                shoppingCartService.updateById(one);
                return R.success("添加购物车信息成功");
            }
        }

        // 4. 若数据库不含有该菜品购物车数据，则存储新数据
        shoppingCartService.save(shoppingCart);

        // 5.返回
        return R.success("添加购物车信息成功");
    }

    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart) {
        // 1.获取当前用户id
        Long userId = BaseContext.getSessionUserId();

        // 2.获取已有的购物车菜品/套餐信息
        ShoppingCart one = null;

        if (shoppingCart.getDishId() != null) {
            // 2.1该购物车数据包含的是菜品
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShoppingCart::getUserId, userId);
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());

            one = shoppingCartService.getOne(queryWrapper);
        }

        if (shoppingCart.getSetmealId() != null) {
            // 2.1该购物车数据包含的是套餐
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShoppingCart::getUserId, userId);
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());

            one = shoppingCartService.getOne(queryWrapper);
        }

        // 3.减小购物车数量
        if (one != null) {
            one.setNumber(one.getNumber() - 1);
            // 3.1如果数量减小到0，删除对应购物车数据
            if (one.getNumber() == 0) {
                shoppingCartService.removeById(one);
            } else {
                // 3.2数量没减小到0，重新更新数量
                shoppingCartService.updateById(one);
            }
        }

        // 4.返回
        return R.success("更新购物车信息成功");
    }

    @DeleteMapping("/clean")
    public R<String> clean() {
        // 1.获取用户id
        Long userId = BaseContext.getSessionUserId();

        // 2.删除该用户所有购物车信息
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);

        shoppingCartService.remove(queryWrapper);

        // 3.返回
        return R.success("清空购物车信息成功");
    }
}
