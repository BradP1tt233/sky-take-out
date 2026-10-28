package entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 菜品实体，对应 dish 表。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dish implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键，自增。 */
    private Long id;

    /** 菜品名称，唯一。 */
    private String name;

    /** 分类 ID，对应 category_id。 */
    private Long categoryId;

    /** 菜品价格，对应 decimal(10,2)。 */
    private BigDecimal price;

    /** 图片路径。 */
    private String image;

    /** 菜品描述。 */
    private String description;

    /** 售卖状态：1 起售，0 停售。 */
    private Integer status;

    /** 创建时间，对应 create_time。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 最后修改时间，对应 update_time。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 创建人 ID，对应 create_user。 */
    private Long createUser;

    /** 最后修改人 ID，对应 update_user。 */
    private Long updateUser;
}
