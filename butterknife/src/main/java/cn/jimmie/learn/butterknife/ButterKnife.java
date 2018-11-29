package cn.jimmie.learn.butterknife;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.view.View;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ButterKnife {
    private static final Map<Class<?>, Constructor<? extends Unbinder>> BINDINGS = new LinkedHashMap<>();

    @UiThread
    public static Unbinder bind(@NonNull Activity target) {
        View sourceView = target.getWindow().getDecorView();
        return bind(target, sourceView);
    }

    @UiThread
    public static Unbinder bind(@NonNull Object target, @NonNull View source) {
        Class<?> targetClass = target.getClass();
        Constructor<? extends Unbinder> constructor = findBindingConstructorForClass(targetClass);

        if (constructor == null) {
            return Unbinder.EMPTY;
        }

        try {
            return constructor.newInstance(target, source);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create binding instance.", e.getCause());
        }
    }

    @UiThread
    private static Constructor<? extends Unbinder> findBindingConstructorForClass(Class<?> cls) {
        Constructor<? extends Unbinder> bindingCtor = BINDINGS.get(cls);
        if (bindingCtor != null) {
            return bindingCtor;
        }
        String clsName = cls.getName();
        try {

            Class<?> bindingClass = Objects.requireNonNull(cls.getClassLoader()).loadClass(clsName + "Unbinder");
            //noinspection unchecked
            bindingCtor = (Constructor<? extends Unbinder>) bindingClass.getConstructor(cls, View.class);
        } catch (Exception e) {
            throw new RuntimeException("Unable to find binding constructor for " + clsName, e);
        }
        BINDINGS.put(cls, bindingCtor);
        return bindingCtor;
    }
}