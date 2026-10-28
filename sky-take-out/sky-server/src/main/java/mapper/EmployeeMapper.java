package mapper;

import annotation.AutoFill;
import dto.EmployeePageQueryDTO;
import entity.Employee;
import enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EmployeeMapper {
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    @Update("update employee set password = #{password} where username = #{username} and password = #{oldPassword}")
    int updatePassword(@Param("username") String username, @Param("password") String password,
                       @Param("oldPassword") String oldPassword);
    @AutoFill(OperationType.INSERT)
    @Insert("insert into employee (name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user) " +
            "values (#{name}, #{username}, #{password}, #{phone}, #{sex}, #{idNumber}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Employee employee);

    List<Employee> selectPage(EmployeePageQueryDTO employeePageQueryDTO);

    @Select("select * from employee where id = ${id}")
    public Employee getById(long id);
   @AutoFill(OperationType.UPDATE)
    public void update(Employee employee);
}

