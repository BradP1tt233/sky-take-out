package mapper;

import annotation.AutoFill;
import entity.Dish;
import enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
@Mapper
/** 分类删除前的关联数量查询。 */
public interface DishMapper {
    @Select("select count(*) from dish where category_id = #{categoryId}")
    int getCountByCategoryId(@Param("categoryId") Long categoryId);
@AutoFill(OperationType.INSERT)
    void insert(Dish dish);
}
