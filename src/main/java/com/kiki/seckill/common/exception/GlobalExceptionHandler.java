package com.kiki.seckill.common.exception;

import com.kiki.seckill.common.result.CodeMsg;
import com.kiki.seckill.common.result.Result;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@ControllerAdvice  //会对所有@RequestMapping方法进行检查,拦截,并进行异常处理。
@ResponseBody
public class GlobalExceptionHandler {
    //拦截什么异常
    @ExceptionHandler(value=Exception.class)//标注要被拦截的异常,此处拦截所有的异常
    public Result<String> exceptionHandler(HttpServletRequest request, Exception e){
        e.printStackTrace();  //在命令行打印异常信息在程序中出错的位置及原因
        if(e instanceof GlobalException) {
            GlobalException ex=(GlobalException) e;
            CodeMsg cm=ex.getCm();
            return Result.error(cm);
        }
        if(e instanceof BindException) {//是绑定异常的情况
            //强转
            BindException ex=(BindException) e;
            //获取错误信息
            List<ObjectError> errors=ex.getAllErrors();
            ObjectError error=errors.get(0);
            String msg=error.getDefaultMessage();
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));
        }else {//不是绑定异常的情况
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }
}
