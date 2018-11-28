package cn.jimmie.learn.butterknife;

/**
 * FUCTION :
 * Created by jimmie.qian on 2018/11/23.
 */
public interface Unbinder {
   Unbinder EMPTY = () -> {
    };

    void unbind();
}
