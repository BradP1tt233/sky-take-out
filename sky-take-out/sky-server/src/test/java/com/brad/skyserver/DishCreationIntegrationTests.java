package com.brad.skyserver;

import com.brad.skyserver.security.EmployeeTokenService;
import context.BaseContext;
import dto.DishDTO;
import entity.DishFlavor;
import entity.Employee;
import mapper.DishFlavorMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import service.DishService;
import utils.AliOssUtil;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"sky.alioss.enabled=false", "sky.jwt.admin-secret-key=dish-creation-test-secret-at-least-32-bytes"})
@AutoConfigureMockMvc
@Transactional
class DishCreationIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired EmployeeTokenService tokens;
    @Autowired DishService service;
    @MockitoBean AliOssUtil uploadTool;
    @MockitoSpyBean DishFlavorMapper flavorMapper;
    String name;
    String token;
    long categoryId;

    @BeforeEach void prepare() {
        name = "dish_reg_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        jdbc.update("insert into category(name,type,sort,status) values (?,1,1,1)", name + "_c");
        categoryId = jdbc.queryForObject("select id from category where name=?", Long.class, name + "_c");
        Employee employee = new Employee(); employee.setId(53L);
        token = tokens.createToken(employee);
        BaseContext.setUserId(53L);
    }
    @AfterEach void clearIdentity() {
        BaseContext.removeUserId();
        if (!org.springframework.test.context.transaction.TestTransaction.isActive() && categoryId > 0) {
            jdbc.update("delete from category where id=? and name=?", categoryId, name + "_c");
        }
    }
    @AfterTransaction void verifyRollback() {
        assertEquals(0, jdbc.queryForObject("select count(*) from dish where name=?", Integer.class, name));
    }
    private String body(String flavors) {
        return "{\"categoryId\":" + categoryId + ",\"name\":\"" + name
                + "\",\"price\":15.00,\"image\":\"dish/test.jpg\",\"status\":0"
                + (flavors == null ? "" : ",\"flavors\":" + flavors) + "}";
    }
    private void create(String flavors) throws Exception {
        mvc.perform(post("/admin/dish").header("token", token).contentType("application/json").content(body(flavors)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(1));
    }
    @Test void emptyFlavorArraySavesDishWithoutFlavorRows() throws Exception {
        create("[]");
        var row = jdbc.queryForMap("select id,create_time,update_time,create_user,update_user from dish where name=?", name);
        assertNotNull(row.get("id"));
        assertNotNull(row.get("create_time")); assertNotNull(row.get("update_time"));
        assertEquals(53L, ((Number)row.get("create_user")).longValue());
        assertEquals(0, jdbc.queryForObject("select count(*) from dish_flavor where dish_id=?", Integer.class, row.get("id")));
    }
    @Test void omittedFlavorsAreAllowed() throws Exception {
        create(null);
        assertEquals(1, jdbc.queryForObject("select count(*) from dish where name=?", Integer.class, name));
    }
    @Test void flavorRowsUseGeneratedDishId() throws Exception {
        create("[{\"name\":\"辣度\",\"value\":\"[微辣]\"},{\"name\":\"温度\",\"value\":\"[热]\"}]");
        assertEquals(2, jdbc.queryForObject("select count(*) from dish_flavor f join dish d on f.dish_id=d.id where d.name=?", Integer.class, name));
    }
    @Test void duplicateDishNameReturnsSpecificConflictMessage() throws Exception {
        create("[]");
        mvc.perform(post("/admin/dish").header("token", token).contentType("application/json").content(body("[]")))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.msg").value("菜品名称已存在"));
    }
    @Test
    @Transactional(propagation=Propagation.NOT_SUPPORTED)
    void flavorInsertFailureRollsBackDishInsert() {
        DishDTO dto = new DishDTO();
        dto.setName(name); dto.setCategoryId(categoryId); dto.setPrice(new BigDecimal("15.00"));
        dto.setImage("dish/test.jpg"); dto.setStatus(0);
        DishFlavor flavor = new DishFlavor(); flavor.setName("辣度"); flavor.setValue("[微辣]");
        dto.setFlavors(List.of(flavor));
        doThrow(new IllegalStateException("simulated flavor insertion failure")).when(flavorMapper).insertBatch(anyList());
        try {
            assertThrows(IllegalStateException.class, () -> service.save(dto));
            assertEquals(0, jdbc.queryForObject("select count(*) from dish where name=?", Integer.class, name));
        } finally {
            // 仅在回归测试失败时，清理本用例唯一名称对应的临时菜品。
            jdbc.update("delete from dish where name=?", name);
        }
    }
}
