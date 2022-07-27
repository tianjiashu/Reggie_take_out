package com.TJS.reggie.Service.Impl;

import com.TJS.reggie.Mapper.OrderMapper;
import com.TJS.reggie.Service.OrdersService;
import com.TJS.reggie.domain.Orders;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrdersService {
}
