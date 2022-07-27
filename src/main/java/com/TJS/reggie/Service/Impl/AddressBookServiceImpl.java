package com.TJS.reggie.Service.Impl;

import com.TJS.reggie.Mapper.AddressBookMapper;
import com.TJS.reggie.Service.AddressBookService;
import com.TJS.reggie.domain.AddressBook;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

}
