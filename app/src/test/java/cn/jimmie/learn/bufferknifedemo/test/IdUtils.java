package cn.jimmie.learn.bufferknifedemo.test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * FUCTION : 反射根据int 数值, 获取 资源名的方法
 * Created by jimmie.qian on 2018/11/23.
 */
public final class IdUtils {
    private final static Map<Integer, String> idsMap = new HashMap<>();
    private static Field[] fields = null;
    private static Class<?> cls = null;

    public static String getIdName(int id) {
        String name = idsMap.get(id);
        if (name != null) return name;
        if (fields == null) {
            fields = cls.getDeclaredFields();
        }

        for (Field field : fields) {
            String n = field.getName();
            int v = 0;
            try {
                v = (int) field.get(field.getName());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (v == id) {
                name = "R.id." + n;
                idsMap.put(id, n);
                return name;
            }
        }
        return null;
    }

    public static String trySearch(String pkg) {
        if (cls != null) return cls.getPackage().getName();
        String clsName = pkg + ".R$id";
        try {
            cls = Class.forName(clsName);
            return cls.getPackage().getName();
        } catch (Exception e) {
            int index = pkg.lastIndexOf(".");
            if (index < 0) return null;
            return trySearch(pkg.substring(0, index));
        }
    }
}
