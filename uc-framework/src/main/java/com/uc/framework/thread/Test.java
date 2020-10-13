package com.uc.framework.thread;

import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) {

        final List<String> items = new ArrayList<String>();

        for (int i = 0; i < 1000; i++) {
            items.add("aaaaaaaaa");
        }
        //
        // long t1 = System.currentTimeMillis();
        // List<String> rs = service(items);
        //
        // System.err.println(" 没开线程: " + (System.currentTimeMillis() - t1) +
        // " size:" + rs.size());

        while (true) {
            new Thread() {
                public void run() {
                    long t1 = System.currentTimeMillis();
                    MultiTasker tasker = AsyncTask.newMultiTasker();
                    ArrayList<String> result = tasker.mapReduce(items, 50,
                            new TaskerCallback<String, String>() {
                                @Override
                                public List<String> run(List<String> curDatas) {
                                    // TODO Auto-generated method stub
                                    return service(curDatas);
                                }
                            });
                    System.err.println(" 开线程: " + (System.currentTimeMillis() - t1) + "  size: "
                            + result.size());
                };
            }.start();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // t1 = System.currentTimeMillis();
        // tasker.map(items, 2, new TaskerCallbackNoneResult<String>() {
        // @Override
        // public void run(List<String> curDatas) {
        // service(curDatas);
        // }
        // });
        // System.err.println(" 111开线程: " + (System.currentTimeMillis() - t1) +
        // "  size: " + result.size());
    }

    private static List<String> service(List<String> curDatas) {
        if (System.currentTimeMillis() % 1023 == 0) {
            System.err.println("空指针");
            throw new NullPointerException();
        }
        List<String> list = new ArrayList<String>();
        for (String g : curDatas) {
            list.add(toUp(g));
        }
        return list;
    }

    private static String toUp(String g) {
        try {
            Thread.sleep(2);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return g.toUpperCase();
    }
}
