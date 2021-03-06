package cn.zmy.easymessenger.compiler;

import java.util.List;

/**
 * Created by zmy on 2018/4/8.
 */
public class ClassHelper
{
    public static boolean isThirdPartyClass(String className)
    {
        return className.contains(".") && !className.startsWith("java");
    }

    public static boolean isList(String className)
    {
        return className.startsWith(List.class.getCanonicalName());
    }
}
