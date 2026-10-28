package entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** 菜品口味实体，对应 dish_flavor 表。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishFlavor implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键，自增。 */
    private Long id;

    /** 菜品 ID，对应 dish_id。 */
    private Long dishId;

    /** 口味名称。 */
    private String name;

    /** 口味值，对应 varchar(255)。 */
    private String value;
}
