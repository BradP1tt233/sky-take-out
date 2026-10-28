package service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import context.BaseContext;
import dto.EmployeeDTO;
import dto.EmployeePageQueryDTO;
import dto.EmployeeLoginDTO;
import exception.BusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.dao.DuplicateKeyException;
import entity.Employee;
import mapper.EmployeeMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import result.PageResult;
import service.EmployeeService;

import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeMapper employeeMapper;

    private final AuthenticationManager authenticationManager;

    public EmployeeServiceImpl(EmployeeMapper employeeMapper, AuthenticationManager authenticationManager) {
        this.employeeMapper = employeeMapper;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Employee login(EmployeeLoginDTO loginDTO) {
        authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(
                loginDTO.getUsername(), loginDTO.getPassword()));
        return employeeMapper.getByUsername(loginDTO.getUsername());
    }

    @Override
    public boolean save(Employee employee) {
        Employee em = employeeMapper.getByUsername(employee.getUsername());
        if(em!=null){
            throw new BusinessException(employee.getUsername()+"已被注册");
        }
        return employeeMapper.insert(employee) > 0;
    }
    @Override
    public PageResult<Employee> page(EmployeePageQueryDTO employeePageQueryDTO){
      Page<Employee> employeePage = PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPagesize())
              .doSelectPage(()->employeeMapper.selectPage(employeePageQueryDTO));
      return new PageResult<Employee>(employeePage.getTotal(),employeePage.getResult(),employeePage.getPageSize(),employeePage.getPageNum());
    }
    @Override
    public void update(EmployeeDTO employeeDTO){
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);
        employee.setUpdateUser(BaseContext.getUserId());
        employee.setUpdateTime(LocalDateTime.now());
        employeeMapper.update(employee);
    }
    @Override
    public Employee getById(Long id){
        if(id==null||id==' '){
            throw new BusinessException("id不正确");
        }
        Employee employee = employeeMapper.getById(id);
        if(employee==null){
            throw new BusinessException("该员工不存在");
        }
        employee.setPassword("****");//查询返回的密码不名文显示
        return employee;
    }
}
