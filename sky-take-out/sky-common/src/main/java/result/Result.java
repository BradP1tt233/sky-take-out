package result;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String msg;
    private Object data;
    public static Result success(){
        Result result = new Result();
        result.code = 1;
        result.msg = "success";
        result.data = "";
        return result;
    }
    public static<T> Result<T> success(T data){
        Result<T> result = new Result<>();
        result.code = 1;
        result.msg = "success";
        result.data = data;
        return result;
    }
    public static Result error(){
        Result result = new Result();
        result.code = 0;
        result.msg = "error";
        result.data = "";
        return result;
    }
    public static Result error(String msg){
        Result result = new Result();
        result.code = 0;
        result.msg = msg;
        result.data = "";
        return result;
    }
}
