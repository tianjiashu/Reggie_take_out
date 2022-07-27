package com.TJS.reggie.Service.Impl;

import com.TJS.reggie.Mapper.UserMapper;
import com.TJS.reggie.Service.UserService;
import com.TJS.reggie.domain.User;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
