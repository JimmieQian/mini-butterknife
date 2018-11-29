package cn.jimmie.learn.butterknife_processor;

import com.squareup.javapoet.ClassName;

/**
 * FUCTION :
 * Created by jimmie.qian on 2018/11/23.
 */
public class FieldBinding {
    private int value;
    private ClassName fieldType;
    private String fieldName;

    public FieldBinding(int value, ClassName fieldType, String name) {
        this.value = value;
        this.fieldType = fieldType;
        this.fieldName = name;
    }

    public int getValue() {
        return value;
    }

    public ClassName getFieldType() {
        return fieldType;
    }

    public String getFieldName() {
        return fieldName;
    }
}
