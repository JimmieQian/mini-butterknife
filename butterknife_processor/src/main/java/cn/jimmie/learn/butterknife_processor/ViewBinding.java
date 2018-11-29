package cn.jimmie.learn.butterknife_processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

import javafx.util.Pair;

/**
 * FUCTION :
 * Created by jimmie.qian on 2018/11/23.
 */
class ViewBinding {
    private static final ClassName UTILS = ClassName.get("cn.jimmie.learn.butterknife", "Utils");
    private static final ClassName VIEW = ClassName.get("android.view", "View");
    private static final ClassName UNBINDER = ClassName.get("cn.jimmie.learn.butterknife", "Unbinder");
    private static final ClassName LISTENER = ClassName.get("cn.jimmie.learn.butterknife", "DebouncingOnClickListener");
    private static final ClassName ILLEGAL_STATE_EXCEPTION = ClassName.get("java.lang", "IllegalStateException");

    private List<FieldBinding> fieldBindings = new ArrayList<>();
    private List<MethodBinding> methodBindings = new ArrayList<>();
    private ClassName target;

    ViewBinding(ClassName target) {
        this.target = target;
    }

    void addFiled(FieldBinding field) {
        fieldBindings.add(field);
    }

    void addMethod(MethodBinding method) {
        methodBindings.add(method);
    }


    JavaFile brewJava() {
        // 单参数构造函数 $ctor(Object target)
        MethodSpec ctor1 = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(target, "target")
                .addStatement("this(target, target.getWindow().getDecorView())")
                .build();

        // 构建两个参数的构造函数 $ctor(Object target,View source)
        MethodSpec.Builder ctorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(target, "target", Modifier.FINAL)
                .addParameter(VIEW, "source")
                .addStatement("this.$N = $N", "target", "target");

        // 解绑器
        MethodSpec.Builder unbind = MethodSpec.methodBuilder("unbind")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addStatement("if (target == null) throw new $T(\"Bindings already cleared.\")", ILLEGAL_STATE_EXCEPTION);

        // 类构建器
        TypeSpec.Builder clsBuilder = TypeSpec.classBuilder(target.simpleName() + "Unbinder")
                .addSuperinterface(UNBINDER)
                .addModifiers(Modifier.PUBLIC)
                .addField(target, "target", Modifier.PRIVATE)
                .addMethod(ctor1);


        // 方法和字段绑定
        List<Pair<FieldBinding, MethodBinding>> fieldMethods = merge();

        for (Pair<FieldBinding, MethodBinding> fieldMethod : fieldMethods) {
            if (fieldMethod == null) continue;
            FieldBinding field = fieldMethod.getKey();
            MethodBinding method = fieldMethod.getValue();

            // 只绑定字段
            if (field != null && method == null)
                brewOnlyField(field, ctorBuilder, unbind);
                // 只绑定方法
            else if (field == null && method != null)
                brewOnlyMethod(method, ctorBuilder, clsBuilder, unbind);
                // 绑定 字段和方法
            else brewFieldMethod(field, method, ctorBuilder, unbind);
        }

        unbind.addStatement("target = null");

        clsBuilder.addMethod(ctorBuilder.build())
                .addMethod(unbind.build());

        return JavaFile.builder(target.packageName(), clsBuilder.build())
                .build();
    }

    private void brewOnlyField(FieldBinding field, MethodSpec.Builder ctorBuilder, MethodSpec.Builder unbind) {
        ClassName fieldType = field.getFieldType();
        ctorBuilder.addStatement("target.$L = $T.findRequiredViewAsType(source, $L, \"field '$L'\", $T.class)",
                field.getFieldName(), UTILS, field.getValue(), field.getFieldName(), fieldType);
        unbind.addStatement("target.$L = null", field.getFieldName());
    }

    private void brewOnlyMethod(MethodBinding method, MethodSpec.Builder ctorBuilder, TypeSpec.Builder clsBuilder, MethodSpec.Builder unbind) {
        String methodName = method.getMethodName();
        int value = method.getValue();
        String member = "view" + value;
        TypeSpec anonymous = brewAnonymousClass(methodName, method.hasParam());

        // 添加成员变量
        clsBuilder.addField(VIEW, member, Modifier.PRIVATE);
        // view7f080024 = Utils.findRequiredView(source, R.id.btn, "method 'click'");
        ctorBuilder.addStatement("$L = $T.findRequiredView(source, $L, \"method 'click'\")",
                member, UTILS, value);
        ctorBuilder.addStatement("$L.setOnClickListener($L)", member, anonymous);

        unbind.addStatement("$L.setOnClickListener(null)", member);
        unbind.addStatement("$L = null", member);
    }

    private void brewFieldMethod(FieldBinding field, MethodBinding method, MethodSpec.Builder ctorBuilder, MethodSpec.Builder unbind) {
        ClassName fieldType = field.getFieldType();
        String methodName = method.getMethodName();

        ctorBuilder.addStatement("target.$L = $T.findRequiredViewAsType(source, $L, \"field '$L' and method '$L'\", $T.class)",
                field.getFieldName(), UTILS, field.getValue(), field.getFieldName(), methodName, fieldType);

        TypeSpec anonymous = brewAnonymousClass(methodName, method.hasParam());

        // target.view.setOnClickListener(new DebouncingOnClickListener() {
        ctorBuilder.addStatement("target.$L.setOnClickListener($L)", field.getFieldName(), anonymous);

        unbind.addStatement("target.$L.setOnClickListener(null)", field.getFieldName());
        unbind.addStatement("target.$L = null", field.getFieldName());
    }

    private TypeSpec brewAnonymousClass(String name, boolean hasParam) {
        String statement = hasParam ? "target.$L(v)" : "target.$L()";
        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(LISTENER)
                .addMethod(MethodSpec.methodBuilder("doClick")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(VIEW, "v")
                        .addStatement(statement, name)
                        .build())
                .build();
    }

    // 合并 字段绑定 和 方法绑定相关
    private List<Pair<FieldBinding, MethodBinding>> merge() {
        List<Pair<FieldBinding, MethodBinding>> list = new ArrayList<>();
        boolean hasAdd = false;

        for (FieldBinding fieldBinding : fieldBindings) {
            for (MethodBinding methodBinding : methodBindings) {
                if (fieldBinding.getValue() == methodBinding.getValue()) {
                    list.add(new Pair<>(fieldBinding, methodBinding));
                    hasAdd = true;
                    break;
                }
            }
            if (!hasAdd) list.add(new Pair<>(fieldBinding, null));
            else hasAdd = false;
        }

        for (MethodBinding methodBinding : methodBindings) {
            for (FieldBinding fieldBinding : fieldBindings) {
                if (fieldBinding.getValue() == methodBinding.getValue()) {
                    hasAdd = true;
                    break;
                }
            }
            if (!hasAdd) list.add(new Pair<>(null, methodBinding));
            else hasAdd = false;
        }
        return list;
    }
}
