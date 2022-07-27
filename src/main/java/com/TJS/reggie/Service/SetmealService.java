package com.TJS.reggie.Service;

import com.TJS.reggie.Dto.SetmealDto;
import com.TJS.reggie.domain.Setmeal;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    void saveWithmeal(SetmealDto dto);
    void deleteWithdish(List<Long> ids);
}
