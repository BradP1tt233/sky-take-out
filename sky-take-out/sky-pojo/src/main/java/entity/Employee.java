package entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工实体，对应 employee 表。
 */
@Data
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键，自增。 */
    private Long id;

    /** 姓名。 */
    private String name;

    /** 用户名，唯一。 */
    private String username;

    /** 密码。 */
    @ToString.Exclude
    private String password;

    /** 手机号。 */
    private String phone;

    /** 性别。 */
    private String sex;

    /** 身份证号，对应 id_number。 */
    private String idNumber;

    /** 账号状态：1 正常，0 锁定。 */
    private Integer status;

    /** 创建时间，对应 create_time。 */
   @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 最后修改时间，对应 update_time。 */
@JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 创建人 ID，对应 create_user。 */
    private Long createUser;

    /** 最后修改人 ID，对应 update_user。 */
    private Long updateUser;
}
