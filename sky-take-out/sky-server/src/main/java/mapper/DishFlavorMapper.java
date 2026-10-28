package mapper;

import entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    /** 批量插入菜品口味；调用前应确保列表非空，且已设置每项的 dishId。 */
    void insertBatch(@Param("flavors") List<DishFlavor> flavors);
}
