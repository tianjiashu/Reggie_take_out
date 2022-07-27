package com.TJS.reggie.Service.Impl;

import com.TJS.reggie.Dto.DishDto;
import com.TJS.reggie.Mapper.DishMapper;
import com.TJS.reggie.Service.DishFlavorService;
import com.TJS.reggie.Service.DishService;
import com.TJS.reggie.domain.Dish;
import com.TJS.reggie.domain.DishFlavor;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    /**
     * 需要操作两张表，因此需要事务控制
     * @param dishDto
     */
    @Autowired
    private DishFlavorService dishFlavorService;


    @Override
    @Transactional()
    public void SaveWithDishFlavor(DishDto dishDto) {
        this.save(dishDto);//先保存到dish表

        Long dishId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId);
        }
        dishFlavorService.saveBatch(flavors);//存dishflavor
    }

    @Override
    public DishDto GetWithDishFlavor(Long id) {
        //先根据id查dish
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();

        BeanUtils.copyProperties(dish,dishDto);

        //根据id查dishflavor
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(dishFlavors);
        return dishDto;
    }

    @Override
    @Transactional
    public void UpdateWithDishFlavor(DishDto dishDto) {
        //先更新菜品表
        this.updateById(dishDto);//这个需要好好解释一下。
        //然后再更新口味表
        Long dishID = dishDto.getId();
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dishID!=null,DishFlavor::getDishId,dishID);
        dishFlavorService.remove(queryWrapper);//先删除旧的

        //再插入新的
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor : flavors) {
            //发送的flovers中没有dishID，只有name和value
            flavor.setDishId(dishID);
        }
        dishFlavorService.saveBatch(flavors);
    }
}
