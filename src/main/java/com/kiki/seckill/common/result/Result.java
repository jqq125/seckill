package com.kiki.seckill.common.result;
/**
* 返回的data不知道是什么类型，故定义一个泛型
*
**/
//定义泛型的类
public class Result<T> {
    private int code;
    private String msg;
    private T data;
    //success
    private Result(T data) {
        this.code=0;
        this.msg="success";
        this.data=data;
    }
    //error
    private Result(CodeMsg cm) {
        if(cm==null) {
            return;
        }

        this.code=cm.getCode();
        this.msg=cm.getMsg();
    }

    //成功
    public static<T> Result<T> success(T data){
        return new Result<T>(data) ;
    }

    // 失败
    //静态方法返回泛型数据时，需要采用static<T>
    public static<T> Result<T> error(CodeMsg sm) {//CodeMsg包含了code和msg
        return new Result<T>(sm);
    }

    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
    public T getData() {
        return data;
    }
    public void setData(T data) {
        this.data = data;
    }
}
