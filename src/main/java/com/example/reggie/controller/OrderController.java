package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.reggie.common.R;
import com.example.reggie.common.util.BaseContext;
import com.example.reggie.entity.*;
import com.example.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {
    private String addressTemplate = "%s(省) %s(市) %s(区域)";
    @Autowired
    UserService userService;
    @Autowired
    ShoppingCartService shoppingCartService;
    @Autowired
    AddressBookService addressBookService;
    @Autowired
    OrderDetailService orderDetailService;
    @Autowired
    OrdersService ordersService;

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        // 1.根据userId获取用户信息
        Long sessionUserId = BaseContext.getSessionUserId();
        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(User::getId, sessionUserId);

        User user = userService.getOne(userQuery);

        // 2.获取用户购物车信息
        LambdaQueryWrapper<ShoppingCart> shoppingCartQuery = new LambdaQueryWrapper<>();
        shoppingCartQuery.eq(ShoppingCart::getUserId, sessionUserId);

        List<ShoppingCart> shoppingCarts = shoppingCartService.list(shoppingCartQuery);

        // 3.计算订单总金额
        AtomicLong amount = new AtomicLong(0);
        shoppingCarts.stream().forEach((item -> {
            amount.addAndGet((item.getAmount().multiply(new BigDecimal(item.getNumber()))).intValue());
        }));

        // 4.根据addressId获取用户地址信息
        LambdaQueryWrapper<AddressBook> addressBookQuery = new LambdaQueryWrapper<>();
        addressBookQuery.eq(AddressBook::getId, orders.getAddressBookId());

        AddressBook addressBook = addressBookService.getOne(addressBookQuery);

        // 5.设置orders一些基础信息
        orders.setId(IdWorker.getId());
        orders.setUserId(sessionUserId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setAddress(String.format(addressTemplate,
                addressBook.getProvinceName(), addressBook.getCityName(), addressBook.getDistrictName()));
        orders.setAmount(new BigDecimal(amount.longValue()));
        orders.setConsignee(user.getName());
        orders.setNumber(String.valueOf(orders.getId()));
        orders.setStatus(0);
        orders.setPhone(user.getPhone());

        // 6.创建订单
        ordersService.save(orders);

        // 6.创建订单明细列表
        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setAmount(item.getAmount().multiply(new BigDecimal(item.getNumber())));
            orderDetail.setOrderId(orders.getId());
            orderDetail.setNumber(item.getNumber());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            return orderDetail;
        })).collect(Collectors.toList());

        // 7.批量存储订单明细
        orderDetailService.saveBatch(orderDetails);

        // 8.清空购物车
        LambdaQueryWrapper<ShoppingCart> cleanQueryWrapper = new LambdaQueryWrapper<>();
        cleanQueryWrapper.eq(ShoppingCart::getUserId, sessionUserId);

        shoppingCartService.remove(cleanQueryWrapper);

        // 9.返回结果
        return R.success("订单创建成功");
    }

}
