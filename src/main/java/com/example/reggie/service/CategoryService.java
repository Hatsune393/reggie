package com.example.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.reggie.entity.Category;

public interface CategoryService extends IService<Category> {
    /***
     * 进行关联性检查，检查通过则删除Category，否则抛出业务异常
     * @param id
     * @return
     */
    boolean checkAndRemoveById(Long id);
}
