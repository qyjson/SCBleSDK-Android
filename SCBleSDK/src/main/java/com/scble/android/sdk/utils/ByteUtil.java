package com.scble.android.sdk.utils;

import androidx.annotation.RestrictTo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * byte[]相关工具类
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class ByteUtil {

    /**
     * 转16进制
     *
     * @param bytes
     * @return
     */
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%02X ", bytes[i]));
        }
        return sb.toString().trim();
    }

    /**
     * 字符串转byte[] 每2个分割
     *
     * @param input
     * @return
     */
    public static byte[] splitStringToByteArray(String input) {
        int length = input.length();
        byte[] result = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            result[i / 2] = (byte) ((Character.digit(input.charAt(i), 16) << 4)
                    + Character.digit(input.charAt(i + 1), 16));
        }
        return result;
    }

    /**
     * 16进制String数组转10进制Int数组
     *
     * @param hexStringArray
     * @return
     */
    public static int[] strArrToIntArrByHex(String[] hexStringArray) {
        int[] decimalArray = new int[hexStringArray.length];
        for (int i = 0; i < hexStringArray.length; i++) {
            decimalArray[i] = Integer.parseInt(hexStringArray[i], 16);
        }
        return decimalArray;
    }

    /**
     * 16进制字符串转为10进制数
     *
     * @param hexString
     * @return
     */
    public static String hexStrTo10(String hexString) {
        long decimal = 0;
        try {
            decimal = Long.parseLong(hexString, 16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decimal + "";
    }

    /**
     * byte[]转List int
     *
     * @param byteArray
     * @return
     */
    public static List<Integer> byteArrToList(byte[] byteArray) {
        // 将byte数组转换为List<Integer>
        List<Integer> intList = new ArrayList<>();
        for (byte b : byteArray) {
            // 将byte转换为int，注意符号扩展
            int intValue = b & 0xFF;
            intList.add(intValue);
        }
        return intList;
    }

    /**
     * 组数据中整数部位一样数量最多的数据保存下来，其他的剔除
     */
    public static List<String> keepMostFrequentIntegerPart(List<String> originalArray) {
        // 使用Map来统计每个整数部分出现的次数
        Map<Integer, Integer> countMap = new HashMap<>();
        for (String str : originalArray) {
            int integerPart = Integer.parseInt(str.split("\\.")[0]);
            countMap.put(integerPart, countMap.getOrDefault(integerPart, 0) + 1);
        }

        // 找到出现次数最多的整数部分
        int mostFrequentIntegerPart = 0;
        int maxCount = 0;
        for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostFrequentIntegerPart = entry.getKey();
            }
        }

        // 剔除不是出现次数最多的整数部分的字符串
        List<String> newArray = new ArrayList<>();
        for (String str : originalArray) {
            int currentIntegerPart = Integer.parseInt(str.split("\\.")[0]);
            if (currentIntegerPart == mostFrequentIntegerPart) {
                newArray.add(str);
            }
        }

        return newArray;
    }

    /**
     * 计算一组字符串数组 取平均值
     *
     * @param array
     * @return
     */
    public static double calculateAverage(List<String> array) {
        double sum = 0.0;
        int count = 0;
        // 遍历字符串数组
        for (String str : array) {
            // 尝试将字符串转换为double
            try {
                double value = Double.parseDouble(str);
                sum += value; // 累加数值
                count++; // 增加计数
            } catch (NumberFormatException e) {
                // 如果字符串不能转换为数值，则忽略它
                System.out.println("无法转换的字符串: " + str);
            }
        }

        // 如果至少有一个数值，则计算平均值
        if (count > 0) {
            return sum / count;
        } else {
            throw new IllegalArgumentException("数组中没有数值可以计算平均值。");
        }
    }
}
