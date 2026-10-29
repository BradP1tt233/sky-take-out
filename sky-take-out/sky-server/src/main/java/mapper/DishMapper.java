package mapper;

import annotation.AutoFill;
import com.github.pagehelper.Page;
import dto.DishPageQueryDTO;
import entity.Dish;
import enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import vo.DishVO;

@Mapper
/** 分类删除前的关联数量查询。 */
public interface DishMapper {
    @Select("select count(*) from dish where category_id = #{categoryId}")
    int getCountByCategoryId(@Param("categoryId") Long categoryId);
@AutoFill(OperationType.INSERT)
    void insert(Dish dish);

    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);
@Select("select status from dish where id = #{id}")
    Integer selectStatusById(Long id);

@Delete("delete from dish where id = #{id}")
    void delete(Long id);
}
