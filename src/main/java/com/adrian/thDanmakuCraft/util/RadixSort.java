package com.adrian.thDanmakuCraft.util;

import java.util.Arrays;

public class RadixSort {
    /**
     * 基數排序主方法
     * @param arr 待排序的整數數組（可包含負數，需額外處理）
     */
    public static void radixSort(int[] arr) {
        if (arr == null || arr.length <= 1) return;

        // 1. 找到最大值，確定最大位數
        int max = Arrays.stream(arr).max().getAsInt();
        int min = Arrays.stream(arr).min().getAsInt();

        // 處理負數（可選，此示例不處理負數）
        if (min < 0) {
            throw new IllegalArgumentException("基數排序默認不支持負數，需額外處理");
        }

        // 2. 按每一位進行計數排序
        for (int exp = 1; max / exp > 0; exp *= 10) {
            countingSortByDigit(arr, exp);
        }
    }

    /**
     * 基於當前位數的計數排序
     * @param arr 待排序數組
     * @param exp 當前位數（1, 10, 100, ...）
     */
    private static void countingSortByDigit(int[] arr, int exp) {
        int n = arr.length;
        int[] output = new int[n];    // 輸出數組
        int[] count = new int[10];    // 計數數組（0-9）

        // 統計當前位數的頻次
        for (int num : arr) {
            int digit = (num / exp) % 10;
            count[digit]++;
        }

        // 將計數轉換為累積位置
        for (int i = 1; i < 10; i++) {
            count[i] += count[i - 1];
        }

        // 反向填充輸出數組（保證穩定性）
        for (int i = n - 1; i >= 0; i--) {
            int digit = (arr[i] / exp) % 10;
            output[count[digit] - 1] = arr[i];
            count[digit]--;
        }

        // 將排序結果複製回原數組
        System.arraycopy(output, 0, arr, 0, n);
    }

    public static void main(String[] args) {
        // 測試數據（10,000 個 0-9999 的隨機整數）
        int[] arr = new int[10000];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (int) (Math.random() * 10000);
        }

        // 排序並驗證
        radixSort(arr);

        // 檢查是否有序
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < arr[i - 1]) {
                System.out.println("排序失敗");
                return;
            }
        }
        System.out.println("排序成功");
    }
}
