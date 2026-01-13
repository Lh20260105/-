package com.travel.travelsystem.common;

/**
 * 这是一个统一的返回结果包装类
 * 用来告诉前端：操作是否成功、错误信息是什么、数据在哪里
 */
public class Result<T> {
    private Boolean success; // 是否成功
    private String message;  // 提示信息
    private T data;          // 携带的数据

    // 构造方法
    public Result() {}

    // 成功的静态方法
    public static <T> Result<T> success(T data, String msg) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setData(data);
        result.setMessage(msg);
        return result;
    }

    // 失败的静态方法
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setMessage(msg);
        return result;
    }

    // --- Getter 和 Setter (必须要有，否则前端拿不到数据) ---
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}