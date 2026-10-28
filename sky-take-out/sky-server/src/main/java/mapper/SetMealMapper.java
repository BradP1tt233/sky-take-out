package mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 分类删除前的关联数量查询。 */
public interface SetMealMapper {
    @Select("select count(*) from setmeal where category_id = #{categoryId}")
    int getCountByCategoryId(@Param("categoryId") Long categoryId);
}
