package com.TJS.reggie.Dto;

import com.TJS.reggie.domain.Setmeal;
import com.TJS.reggie.domain.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
