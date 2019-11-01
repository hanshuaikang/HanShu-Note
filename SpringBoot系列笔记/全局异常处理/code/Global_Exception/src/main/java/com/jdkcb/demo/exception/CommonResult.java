package com.jdkcb.demo.exception;

public class CommonResult<T> {

    private String errorCode;
    private String errorMsg;
    private int status;



    public static <T> CommonResult<T> errorResult(String errorCode, String errorMsg){
        CommonResult<T> commonResult = new CommonResult<>();
        commonResult.errorCode = errorCode;
        commonResult.errorMsg = errorMsg;
        commonResult.status = -1;
        return commonResult;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }



}
