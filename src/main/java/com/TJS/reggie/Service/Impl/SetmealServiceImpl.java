package com.TJS.reggie.Service.Impl;

import com.TJS.reggie.Dto.SetmealDto;
import com.TJS.reggie.Mapper.SetmealMapper;
import com.TJS.reggie.Service.SetmealDishService;
import com.TJS.reggie.Service.SetmealService;
import com.TJS.reggie.common.CustomException;
import com.TJS.reggie.domain.Setmeal;
import com.TJS.reggie.domain.SetmealDish;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {


    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    @Transactional
    public void saveWithmeal(SetmealDto dto) {
        //先存Setmeal表
        this.save(dto);

        Long dishid = dto.getId();
        //再存SetmealDish表
        List<SetmealDish> dishes = dto.getSetmealDishes();
        for (SetmealDish dish : dishes) {
            dish.setSetmealId(dishid);
        }
        setmealDishService.saveBatch(dishes);
    }

    @Override
    @Transactional
    public void deleteWithdish(List<Long> ids) {
        //若状态为1则不能删除，抛异常
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);
        int count = this.count(queryWrapper);
        if(count>0){
            throw new CustomException("套餐还在售卖，无法删除");
        }
        //若可以删除，则先删除setmeal表
        this.removeByIds(ids);
        //再删除setmeal_dish表
        LambdaQueryWrapper<SetmealDish> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(queryWrapper2);
    }
}
