package com.TJS.reggie.Service;

import com.TJS.reggie.domain.Category;
import com.baomidou.mybatisplus.extension.service.IService;

public interface CategoryService extends IService<Category> {

    void remove(long id);
}
