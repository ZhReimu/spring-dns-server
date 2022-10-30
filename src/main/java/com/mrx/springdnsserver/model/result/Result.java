package com.mrx.springdnsserver.model.result;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 统一响应实体
 *
 * @param <T> data 类型
 * @author Mr.X
 * @since 2022-07-02
 */
@Data
@Slf4j
@SuppressWarnings({"unused", "SameParameterValue"})
public class Result<T> {

    /**
     * 响应 code
     */
    private Integer code;

    /**
     * 响应是否成功
     */
    private Boolean success;

    /**
     * 响应 message
     */
    private String message;

    /**
     * 响应 data
     */
    private T data;

    private Result() {
        this(null, null, null, null);
    }

    private Result(Integer code, Boolean success, String message, T data) {
        this.code = code;
        this.success = success;
        this.message = message;
        if (data instanceof List) {
            log.debug("createResult: {}, data[0]: {}", this, ((List<?>) data).get(0));
        }
        this.data = data;
    }

    @NonNull
    public static Result<String> success(ResponseCode code) {
        return success(code.code, code.msg);
    }

    @NonNull
    public static Result<String> success(int code, String message) {
        return success(code, message, "");
    }

    @NonNull
    public static <T> Result<T> success(int code, String message, T data) {
        return newResult(code, true, message, data);
    }

    @NonNull
    public static <T> Result<T> newResult(int code, boolean success, String message, T data) {
        return new Result<>(code, success, message, data);
    }

    @NonNull
    public static Result<String> success() {
        return success(ResponseCode.SUCCESS.msg, "");
    }

    @NonNull
    public static <T> Result<T> success(String message, T data) {
        return success(ResponseCode.SUCCESS.code, message, data);
    }

    @NonNull
    public static <T> Result<T> success(ResponseCode success, T data) {
        return success(success, success.msg, data);
    }

    @NonNull
    public static <T> Result<T> success(ResponseCode success, String message, T data) {
        return success(success.code, message, data);
    }

    @NonNull
    public static <T> Result<T> success(T data) {
        return success(ResponseCode.SUCCESS.msg, data);
    }

    @NonNull
    public static Result<String> success(String message) {
        return success(message, "");
    }

    @NonNull
    public static Result<String> fail(ResponseCode fail, Throwable t) {
        return newResult(fail.code, fail.msg, t.getMessage());
    }

    @NonNull
    public static <T> Result<T> newResult(int code, String message, T data) {
        return newResult(code, code >= ResponseCode.SUCCESS.code && code < ResponseCode.FAILURE.code, message, data);
    }

    @NonNull
    public static <T> Result<T> fail(ResponseCode fail, T data) {
        return newResult(fail.code, fail.msg, data);
    }

    @NonNull
    public static <T> Result<T> fail(String message, T data) {
        return newResult(ResponseCode.FAILURE.code, message, data);
    }

    @NonNull
    public static Result<String> fail(ResponseCode code) {
        return fail(code.code, code.msg);
    }

    /**
     * 构造一个 fail 响应
     *
     * @param code    code
     * @param message msg
     * @return fail 响应
     */
    @NonNull
    public static Result<String> fail(int code, String message) {
        return fail(code, message, "");
    }

    @NonNull
    public static <T> Result<T> fail(int code, String message, T data) {
        return newResult(code, false, message, data);
    }

    @NonNull
    public static Result<String> fail() {
        return fail(ResponseCode.FAILURE.msg);
    }

    @NonNull
    public static Result<String> fail(String message) {
        return fail(ResponseCode.FAILURE.code, message);
    }

    @NonNull
    public static <T> Result<T> fail(int code, T data) {
        return fail(code, ResponseCode.FAILURE.msg, data);
    }

    @NonNull
    public static Result<String> fail(int code, @NonNull Throwable t) {
        return fail(code, t.getMessage());
    }

    @NonNull
    public static Result<String> fail(@NonNull Throwable t) {
        return fail(ResponseCode.FAILURE.code, t.getMessage());
    }

    @NonNull
    public static Result<String> fail(String message, @NonNull Throwable t) {
        return fail(ResponseCode.FAILURE.code, message, t);
    }

    /**
     * 创建一个常见的 fail 响应, 不打印堆栈
     *
     * @param code    code
     * @param message msg
     * @param t       exception
     * @return fail 响应
     */
    @NonNull
    public static Result<String> fail(int code, String message, @NonNull Throwable t) {
        return fail(message, t, false);
    }

    @NonNull
    public static Result<String> fail(String message, @NonNull Throwable t, boolean printStackTrace) {
        return fail(ResponseCode.FAILURE.code, message, t, printStackTrace);
    }

    @NonNull
    public static Result<String> fail(int code, String message, @NonNull Throwable t, boolean printStackTrace) {
        if (printStackTrace) t.printStackTrace();
        return fail(code, message, t.getMessage());
    }

    public String toJSONString() {
        return toJSONObject().toJSONString();
    }

    public JSONObject toJSONObject() {
        return (JSONObject) JSON.toJSON(this);
    }

}
