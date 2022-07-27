package com.TJS.reggie.Service.Impl;

import com.TJS.reggie.Mapper.OrderDetailMapper;
import com.TJS.reggie.Service.OrderDetailService;
import com.TJS.reggie.domain.OrderDetail;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper,OrderDetail> implements OrderDetailService {
}
