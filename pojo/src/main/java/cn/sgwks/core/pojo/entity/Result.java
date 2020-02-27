package cn.sgwks.core.pojo.entity;

import java.io.Serializable;

public class Result implements Serializable {
    //布尔值,true操作成功,false操作失败
    private boolean success;
    //成功或错误信息
    private String message;
    public Result(boolean success, String message) {
        super();
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
