package cn.jimmie.learn.bufferknifedemo.test;

import org.junit.Test;

import cn.jimmie.learn.bufferknifedemo.IdUtils;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void test1() {
        int i = 0x7f080024;
        String pkg = "cn.jimmie.learn.bufferknifedemo.Main";
        pkg = IdUtils.trySearch(pkg);
        String id = IdUtils.getIdName(i);
        System.out.println(id);
        System.out.println(pkg);
        System.out.println(i);
    }
}