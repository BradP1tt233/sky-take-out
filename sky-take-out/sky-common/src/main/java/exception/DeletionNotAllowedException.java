package exception;

/** 存在业务关联，禁止删除。 */
public class DeletionNotAllowedException extends BusinessException {
    public DeletionNotAllowedException(String message) {
        super(message);
    }
}
