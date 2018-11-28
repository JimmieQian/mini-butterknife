package cn.jimmie.learn.butterknife_processor;

/**
 * FUCTION :
 * Created by jimmie.qian on 2018/11/23.
 */
public class MethodBinding {
    private String methodName;
    private int value;
    private boolean hasParam;

    public MethodBinding(String methodName, int value, boolean hasParam) {
        this.methodName = methodName;
        this.hasParam = hasParam;
        this.value = value;
    }

    public String getMethodName() {
        return methodName;
    }

    public int getValue() {
        return value;
    }

    public boolean hasParam() {
        return hasParam;
    }
}
