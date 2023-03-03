package com.example.datasourceprimordialdemo2.datasource;

/**
 * @author： Aaron
 * @date： 2023-03-01 11:25
 */

public class DynamicDataSourceContextHolder {
    // 线程安全
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    public static void setDataSourceType(String type) {
        CONTEXT_HOLDER.set(type);
    }

    public static String getDataSourceType() {
        return CONTEXT_HOLDER.get();
    }

    public static void clearDataSourceType() {
        CONTEXT_HOLDER.remove();
    }


}
