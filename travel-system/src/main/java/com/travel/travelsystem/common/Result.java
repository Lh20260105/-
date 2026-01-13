package com.travel.travelsystem.common;

/**
 * 统一返回结果类
 */
public class Result<T> {
    private Boolean success; // 是否成功 (true/false)
    private String message;  // 提示消息
    private T data;          // 返回的数据

    // 1. 无参构造方法 (必须保留)
    public Result() {}

    // 2. 全参构造方法：解决你图片里 new Result(...) 报红的问题
    public Result(Boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // --- 静态调用方法 ---

    // 成功：只传数据 (你会用在详情接口)
    public static <T> Result<T> success(T data) {
        // 这里用 true 代替之前的 200，因为字段类型是 Boolean
        return new Result<>(true, "操作成功", data);
    }

    // 成功：传数据 + 自定义消息 (你会用在发布接口)
    public static <T> Result<T> success(T data, String message) {
        return new Result<>(true, message, data);
    }

    // 失败
    public static <T> Result<T> error(String message) {
        return new Result<>(false, message, null);
    }

    // --- Getter 和 Setter (必须保留，否则前端无法解析 JSON) ---

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}