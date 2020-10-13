package com.uc.framework.obj;

import com.uc.framework.Constants;

/**
 * <p>
 * Description: ͨ��ҵ���쳣
 * </p>
 *
 * @author HadLuo
 * @date 2020-9-2 11:01:29
 */
public class BusinessException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = -547369325974918093L;

    private int code;

    public BusinessException(String errorMessage) {
        super(errorMessage);
        this.code = Constants.Default_Err_Code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = Constants.Default_Err_Code;
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

