package com.TJS.reggie.Service.Impl;

import com.TJS.reggie.Mapper.DishFlavorMapper;
import com.TJS.reggie.Service.DishFlavorService;
import com.TJS.reggie.domain.DishFlavor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
