package service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import constant.MessageConstant;
import constant.StatusConstant;
import dto.DishDTO;
import dto.DishPageQueryDTO;
import entity.Dish;
import entity.DishFlavor;
import exception.DeletionNotAllowedException;
import mapper.DishFlavorMapper;
import mapper.DishMapper;
import mapper.SetmealDishMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import result.PageResult;
import service.DishService;
import vo.DishVO;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Override
    @Transactional
    //菜品表插入1条数据
    public void save(DishDTO dishDTO){
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);
        Long dishid = dish.getId();
        //口味表插入多条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishid));
            dishFlavorMapper.insertBatch(flavors);
        }

    }
    public PageResult page(DishPageQueryDTO dishPageQueryDTO){
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page= dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult<>(page.getTotal(),page.getResult(),page.getPageSize(),page.getPageNum());
    }
    @Transactional
    public void deleteBatch(List<Long> ids){
        //1.检查菜品是否处于起售状态
        for (Long id : ids) {
            Integer status = dishMapper.selectStatusById(id);
            if (status == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
            //2.判断菜品是否有关联的套餐
        }
                List<Long> setMealIds = setmealDishMapper.selectSetmealIdByDishId(ids);
                if (setMealIds != null && setMealIds.size() > 0) {
                    throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
                }

          for (Long id : ids){
              //3.删除菜品
              dishMapper.delete(id);
              //4.删除口味
              dishFlavorMapper.delete(id);
          }

        }
    }








