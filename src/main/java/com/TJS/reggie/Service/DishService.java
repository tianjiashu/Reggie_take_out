package com.TJS.reggie.Service;

import com.TJS.reggie.Dto.DishDto;
import com.TJS.reggie.domain.Dish;
import com.baomidou.mybatisplus.extension.service.IService;

public interface DishService extends IService<Dish> {

    void SaveWithDishFlavor(DishDto dishDto);

    DishDto GetWithDishFlavor(Long id);

    void UpdateWithDishFlavor(DishDto dishDto);
}
