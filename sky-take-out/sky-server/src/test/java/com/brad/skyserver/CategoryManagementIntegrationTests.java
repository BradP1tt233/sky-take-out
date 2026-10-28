package com.brad.skyserver;

import com.brad.skyserver.aspect.AutoFillAspect;
import com.brad.skyserver.security.EmployeeTokenService;
import context.BaseContext;
import entity.Employee;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 真实数据库与过滤器链测试；每个用例结束后回滚测试数据。 */
@SpringBootTest(properties = "sky.jwt.admin-secret-key=category-integration-test-secret-32-bytes-minimum")
@AutoConfigureMockMvc
@Transactional
class CategoryManagementIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired EmployeeTokenService tokens;
    @Autowired ApplicationContext context;
    private String token;
    private String name;

    @BeforeEach void prepare() {
        Employee employee = new Employee(); employee.setId(2L);
        token = tokens.createToken(employee);
        name = "it_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }
    @AfterEach void cleanupThread() { BaseContext.removeUserId(); org.springframework.security.core.context.SecurityContextHolder.clearContext(); }
    private long seed(int status) {
        jdbc.update("insert into category (name,type,sort,status,create_time,update_time,create_user,update_user) values (?,1,1,?,'2000-01-01 00:00:00','2000-01-01 00:00:00',99,99)", name, status);
        return jdbc.queryForObject("select id from category where name=?", Long.class, name);
    }
    private String body(Long id, String label) {
        return "{" + (id == null ? "" : "\"id\":" + id + ",")
                + "\"type\":1,\"name\":\"" + label + "\",\"sort\":1}";
    }
    @Test void protectedEndpointRequiresAuthentication() throws Exception {
        mvc.perform(get("/admin/category/list").param("type", "1")).andExpect(status().isUnauthorized());
    }
    @Test void autoFillAspectMustBeRegistered() {
        assertEquals(1, context.getBeansOfType(AutoFillAspect.class).size(), "自动填充切面未被Spring扫描");
    }
    @Test void createMustFillAllAuditFields() throws Exception {
        mvc.perform(post("/admin/category").header("token", token).contentType("application/json").content(body(null, name)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(1));
        var row = jdbc.queryForMap("select * from category where name=?", name);
        assertAll(
            () -> assertEquals(0, ((Number) row.get("status")).intValue()),
            () -> assertNotNull(row.get("create_time"), "创建时间为空"),
            () -> assertNotNull(row.get("update_time"), "修改时间为空"),
            () -> assertEquals(2L, row.get("create_user"), "创建人未填充"),
            () -> assertEquals(2L, row.get("update_user"), "修改人未填充")
        );
    }
    @Test void duplicateCategoryNameIsRejected() throws Exception {
        seed(0);
        mvc.perform(post("/admin/category").header("token", token).contentType("application/json").content(body(null, name)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(0));
    }
    @Test void pageQueryMustReturnMatchingCategory() throws Exception {
        long id = seed(0);
        mvc.perform(get("/admin/category/page").header("token", token).param("name", name).param("page", "1").param("pageSize", "10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(id));
    }
    @Test void editMustPersistChangesAndPreserveCreationAudit() throws Exception {
        long id = seed(0);
        mvc.perform(put("/admin/category").header("token", token).contentType("application/json").content(body(id, name + "_e")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(1));
        var row = jdbc.queryForMap("select * from category where id=?", id);
        assertEquals(name + "_e", row.get("name"));
        assertEquals(99L, row.get("create_user"));
        assertEquals(2L, row.get("update_user"));
        assertEquals(java.time.LocalDateTime.of(2000, 1, 1, 0, 0),
                jdbc.queryForObject("select create_time from category where id=?", java.time.LocalDateTime.class, id));
        assertNotEquals(row.get("create_time"), row.get("update_time"));
    }
    @Test void enableMustPersistStatusAndModifier() throws Exception {
        long id = seed(0);
        mvc.perform(post("/admin/category/status/1").header("token", token).param("id", String.valueOf(id)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(1));
        assertEquals(1, jdbc.queryForObject("select status from category where id=?", Integer.class, id));
        assertEquals(2L, jdbc.queryForObject("select update_user from category where id=?", Long.class, id));
    }
    @Test void disableMustPersistStatus() throws Exception {
        long id = seed(1);
        mvc.perform(post("/admin/category/status/0").header("token", token).param("id", String.valueOf(id)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(1));
        assertEquals(0, jdbc.queryForObject("select status from category where id=?", Integer.class, id));
    }
    @Test void listShowsEnabledCategory() throws Exception {
        seed(1);
        mvc.perform(get("/admin/category/list").header("token", token).param("type", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[?(@.name == '" + name + "')]").isNotEmpty());
    }
    @Test void listHidesDisabledCategory() throws Exception {
        seed(0);
        mvc.perform(get("/admin/category/list").header("token", token).param("type", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[?(@.name == '" + name + "')]").isEmpty());
    }
    @Test void unreferencedCategoryCanBeDeleted() throws Exception {
        long id = seed(0);
        mvc.perform(delete("/admin/category").header("token", token).param("id", String.valueOf(id)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(1));
        assertEquals(0, jdbc.queryForObject("select count(*) from category where id=?", Integer.class, id));
    }
    @Test void referencedCategoryCannotBeDeleted() throws Exception {
        long id = seed(0);
        jdbc.update("insert into dish (name,category_id) values (?,?)", name + "_d", id);
        mvc.perform(delete("/admin/category").header("token", token).param("id", String.valueOf(id)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(0));
        assertEquals(1, jdbc.queryForObject("select count(*) from category where id=?", Integer.class, id));
    }
    @Test void invalidCreateParametersAreRejected() throws Exception {
        mvc.perform(post("/admin/category").header("token", token).contentType("application/json")
                .content("{\"type\":3,\"name\":\" \",\"sort\":1}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(0));
    }
    @Test void invalidStatusAndTypeAreRejected() throws Exception {
        mvc.perform(post("/admin/category/status/2").header("token", token).param("id", "1"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/admin/category/list").header("token", token).param("type", "3"))
                .andExpect(status().isBadRequest());
    }
    private void invokeAspect(entity.Category entity, enumeration.OperationType operation) throws Exception {
        var joinPoint = org.mockito.Mockito.mock(org.aspectj.lang.JoinPoint.class);
        org.mockito.Mockito.when(joinPoint.getArgs()).thenReturn(new Object[] {entity});
        String method = operation == enumeration.OperationType.INSERT ? "addCategory" : "updateCategory";
        var annotation = mapper.CategoryMapper.class.getMethod(method, entity.Category.class)
                .getAnnotation(annotation.AutoFill.class);
        new AutoFillAspect().autoFill(joinPoint, annotation);
    }
    @Test void reflectionFillsInsertWhenUserContextIsProvided() throws Exception {
        BaseContext.setUserId(2L);
        var entity = new entity.Category();
        invokeAspect(entity, enumeration.OperationType.INSERT);
        assertAll(() -> assertEquals(2L, entity.getCreateUser()),
                () -> assertEquals(2L, entity.getUpdateUser()),
                () -> assertNotNull(entity.getCreateTime()),
                () -> assertEquals(entity.getCreateTime(), entity.getUpdateTime()));
    }
    @Test void reflectionPreservesCreationFieldsDuringUpdate() throws Exception {
        BaseContext.setUserId(2L);
        var entity = new entity.Category();
        var oldTime = java.time.LocalDateTime.of(2000, 1, 1, 0, 0);
        entity.setCreateTime(oldTime); entity.setCreateUser(99L);
        invokeAspect(entity, enumeration.OperationType.UPDATE);
        assertEquals(99L, entity.getCreateUser());
        assertEquals(oldTime, entity.getCreateTime());
        assertEquals(2L, entity.getUpdateUser());
        assertNotNull(entity.getUpdateTime());
    }
    @Test void authenticatedSecurityIdentityMustBeAvailableToAspect() {
        var jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("test")
                .header("alg", "HS256").subject("2").build();
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken(jwt,
                        java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("SCOPE_employee"))));
        var entity = new entity.Category();
        var request = new org.springframework.mock.web.MockHttpServletRequest();
        var response = new org.springframework.mock.web.MockHttpServletResponse();
        assertDoesNotThrow(() -> new com.brad.skyserver.security.BaseContextFilter().doFilter(request, response,
                (req, res) -> {
                    assertEquals(2L, BaseContext.getUserId());
                    try { invokeAspect(entity, enumeration.OperationType.INSERT); }
                    catch (Exception exception) { throw new jakarta.servlet.ServletException(exception); }
                }));
        assertEquals(2L, entity.getCreateUser());
        assertNull(BaseContext.getUserId(), "请求结束后应清理登录身份");
    }
}
