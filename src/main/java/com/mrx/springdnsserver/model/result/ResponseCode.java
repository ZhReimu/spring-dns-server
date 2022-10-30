package com.mrx.springdnsserver.model.result;

import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

/**
 * 自定义的响应码枚举<br/>
 * 在系统出现错误时会优先使用这里的错误码以及错误消息进行响应
 *
 * @author Mr.X
 * @since 2022-08-23 16:52
 */
@ToString
@SuppressWarnings("unused")
public enum ResponseCode {
    // 通用状态码
    NOT_FOUND(404, "找不到请求的资源"),

    // 自定义状态码
    // 成功 状态码区间 20000 - 39999
    SUCCESS(20000, "操作成功"),
    REGISTER_SUCCESS(20001, "注册成功"),
    LOGIN_SUCCESS(20002, "登录成功"),

    // 失败 状态码区间 40000 - 59999
    FAILURE(40000, "出现错误"),
    USER_NAME_EXISTS(40001, "用户名已存在"),
    REGISTER_FAILURE(40002, "注册失败"),
    ACCESS_DENIED(40003, "权限不足"),
    SERVER_ERROR(40004, "内部错误"),
    PARAMETER_ERROR(40005, "参数有误"),
    TOKEN_EXPIRED(40006, "token 过期"),
    METHOD_NOT_ALLOWED(40007, "不支持该请求方法"),
    LOGIN_FAILURE(40008, "登录失败, 用户名或密码错误"),
    ;

    public final int code;

    public final String msg;

    ResponseCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 使用错误码获取错误消息<br/>
     * 优先使用 {@link ResponseCode} 类的错误消息 <br/>
     * 若 {@link ResponseCode} 中找不到则会尝试从 {@link HttpStatus} 中查找对应的错误信息<br/>
     * 若还是找不到则抛出异常
     *
     * @param code 错误码
     * @return 错误消息
     */
    @NonNull
    public static String getMsgOfCode(int code) {
        for (ResponseCode responseCode : values()) {
            if (responseCode.code == code) return responseCode.msg;
        }
        try {
            return HttpStatus.valueOf(code).getReasonPhrase();
        } catch (IllegalArgumentException ignored) {

        }
        throw new IllegalArgumentException("找不到错误码!");
    }

}
