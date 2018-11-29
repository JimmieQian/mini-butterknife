package cn.jimmie.learn.butterknife_processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import cn.jimmie.learn.butterknife_annotations.BindView;
import cn.jimmie.learn.butterknife_annotations.OnClick;

// 使用@AutoService注解,避免了在resource文件夹中注册的步骤
@AutoService(Processor.class)
public class ButterKnifeProcessor extends AbstractProcessor {
    private Filer filer;

    /**
     * 初始化, 获取各种工具类
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
    }

    /**
     * 注册感兴趣的注解类
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(BindView.class.getCanonicalName());
        annotations.add(OnClick.class.getCanonicalName());
        return annotations;
    }

    /**
     * 返回使用的java版本
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 扫描、评估和处理注解，以及生成Java文件的逻辑代码
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        boolean shouldWrite = false;
        //每一个 Activity 对应一个 ViewBinding
        Map<String, ViewBinding> bindingMap = new HashMap<>();

        // 绑定ViewId 遍历所有被注解了@BindView的元素
        for (Element element : roundEnv.getElementsAnnotatedWith(BindView.class)) {
            // 判断注释 是否在 字段上
            if (!(element instanceof VariableElement) || element.getKind() != ElementKind.FIELD) {
                throw new IllegalStateException("@BindView annotation must be on a field.");
            }

            // 获取目标类的类型 如(xxx.MainActivity)
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();
            String targetClassName = typeElement.getQualifiedName().toString();

            // 根据类名来生成文件, 一个被注解的类中所有的注解对应生成一份类文件
            ViewBinding binding = bindingMap.get(targetClassName);
            if (binding == null) {
                binding = new ViewBinding(ClassName.bestGuess(targetClassName));
                bindingMap.put(targetClassName, binding);
            }

            // 获取注解上的值(这里是R.id对应的int值)
            int value = element.getAnnotation(BindView.class).value();
            // 获取字段的类型
            ClassName fieldClass = (ClassName) ClassName.get(element.asType());

            // 判断字段类型是否是View的子类型
            if (!Utils.isSubtypeOfType(element.asType(), "android.view.View")) {
                throw new IllegalStateException("field type must be the child of android.view.View");
            }

            // 获取字段的名称
            String name = element.getSimpleName().toString();

            // 添加字段绑定
            FieldBinding field = new FieldBinding(value, fieldClass, name);
            binding.addFiled(field);

            shouldWrite = true;
        }


        // 绑定监听事件 遍历所有被注解了@OnClick的元素
        for (Element element : roundEnv.getElementsAnnotatedWith(OnClick.class)) {
            // 判断注释 是否在 方法上
            if (!(element instanceof ExecutableElement) || element.getKind() != ElementKind.METHOD) {
                throw new IllegalStateException("@Click annotation must be on a method.");
            }

            ExecutableElement executableElement = (ExecutableElement) element;
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();

            // 获取目标类的类型 如(xxx.MainActivity)
            String targetName = typeElement.getQualifiedName().toString();

            // 添加方法绑定
            ViewBinding binding = bindingMap.get(targetName);
            if (binding == null) {
                binding = new ViewBinding(ClassName.bestGuess(targetName));
                bindingMap.put(targetName, binding);
            }

            // 获取方法的参数列表
            List<? extends VariableElement> methodParameters = executableElement.getParameters();
            // 获取方法参数的个数
            int methodParameterSize = methodParameters.size();
            // 参数校验
            if (methodParameterSize > 1) {
                throw new IllegalStateException("@Click method only has one param, and must be android.view.View");
            } else if (methodParameterSize == 1) {
                VariableElement param = methodParameters.get(0);
                TypeName paramType = TypeName.get(param.asType());
                if (!paramType.toString().equals("android.view.View")) {
                    throw new IllegalStateException("@Click method only has one param, and must be android.view.View");
                }
            }

            // 获取返回类型
            TypeName returnType = TypeName.get(executableElement.getReturnType());
            if (!returnType.toString().equals("void")) {
                throw new IllegalStateException("@Click method return type must be void");
            }

            // 获取注解上的值 [R.id.x1,R.id.x2]
            int[] values = element.getAnnotation(OnClick.class).value();
            if (values.length == 0) continue;

            // 获取注解上的方法名
            String methodName = element.getSimpleName().toString();

            // 添加 方法绑定
            for (int value : values) {
                MethodBinding method = new MethodBinding(methodName, value, methodParameterSize == 1);
                binding.addMethod(method);
            }

            shouldWrite = true;
        }

        if (shouldWrite) {
            // 生成java文件
            for (String key : bindingMap.keySet()) {
                ViewBinding binding = bindingMap.get(key);
                JavaFile javaFile = binding.brewJava();
                try {
                    javaFile.writeTo(filer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }
}
