package com.TJS.reggie.Service.Impl;

import com.TJS.reggie.Mapper.CategoryMapper;
import com.TJS.reggie.Service.CategoryService;
import com.TJS.reggie.Service.DishService;
import com.TJS.reggie.Service.SetmealService;
import com.TJS.reggie.common.CustomException;
import com.TJS.reggie.domain.Category;
import com.TJS.reggie.domain.Dish;
import com.TJS.reggie.domain.Setmeal;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {


    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;


    @Override
    public void remove(long id) {
        //看看该分类是否关联了菜品
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Dish::getCategoryId,id);
        int count1 = dishService.count(lqw);
        if(count1>0){
            //关联了相关菜品
            throw new CustomException("该分类已经关联了菜品");
        }
        //看看当前分类是否关联了套餐
        LambdaQueryWrapper<Setmeal> lqw_ = new LambdaQueryWrapper<>();
        lqw_.eq(Setmeal::getCategoryId,id);
        int count2 = setmealService.count(lqw_);
        if(count2>0){
            //关联了相关的套餐。
            throw new CustomException("前分类已经关联了套餐");
        }
        super.removeById(id);
    }
}
