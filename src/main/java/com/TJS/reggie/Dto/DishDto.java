package com.TJS.reggie.Dto;

import com.TJS.reggie.domain.Dish;
import com.TJS.reggie.domain.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;//以下两个字段，在添加菜品的时候没用

    private Integer copies;
}
