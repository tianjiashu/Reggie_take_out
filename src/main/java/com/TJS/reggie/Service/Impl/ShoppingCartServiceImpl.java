package com.TJS.reggie.Service.Impl;

import com.TJS.reggie.Mapper.ShoppingCartMapper;
import com.TJS.reggie.Service.ShoppingCartService;
import com.TJS.reggie.domain.ShoppingCart;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper,ShoppingCart> implements ShoppingCartService {
}
