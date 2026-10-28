package context;

/** 当前请求线程中的登录员工 ID；由安全过滤器设置并在请求结束时清理。 */
public class BaseContext {
    private static final ThreadLocal<Long> CURRENT_ID = new ThreadLocal<>();

    public static void setUserId(long id) {
        CURRENT_ID.set(id);
    }

    /** 未设置登录身份时返回 null。 */
    public static Long getUserId() {
        return CURRENT_ID.get();
    }

    public static void removeUserId() {
        CURRENT_ID.remove();
    }
}
