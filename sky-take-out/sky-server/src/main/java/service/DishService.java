package service;

import dto.DishDTO;
import dto.DishPageQueryDTO;
import result.PageResult;

import java.util.List;

public interface DishService {
    public void save(DishDTO dishDTO);

    PageResult page(DishPageQueryDTO dishPageQueryDTO);

    void deleteBatch(List<Long> ids);
}
