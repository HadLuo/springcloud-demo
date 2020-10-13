package com.uc.framework;

import java.util.UUID;

public class UUIDUtils {

    /**
     * 得到32位的uuid
     *
     * @return
     */
    public static String getUUID32() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }

    /**
     * 得到指定数量的UUID，以数组的形式返回
     *
     * @param num
     * @return
     */
    public static String[] getUUID(int num) {
        if (num <= 0)
            return null;
        String[] uuidArr = new String[num];
        for (int i = 0; i < uuidArr.length; i++) {
            uuidArr[i] = getUUID32();
        }
        return uuidArr;
    }


    public static String[] chars = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

    /**
     * 生成8位UUId
     *
     * @return
     */
    public static  String generateUuid6() {
        StringBuffer shortBuffer = new StringBuffer();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 6; i++) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            shortBuffer.append(chars[x % 10]);
        }
        return shortBuffer.toString();

    }

    public static void main(String[] args) {
        System.out.println(generateUuid6());
    }
}
