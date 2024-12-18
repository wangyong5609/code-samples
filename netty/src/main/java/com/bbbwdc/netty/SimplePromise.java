package com.bbbwdc.netty;

import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class SimplePromise {
    public static void main(String[] args) throws InterruptedException {
        // 创建线程池
        DefaultEventExecutor eventExecutors = new DefaultEventExecutor();
        DefaultPromise promise = new DefaultPromise(eventExecutors);
        // 添加监听器
        promise.addListener((GenericFutureListener<Future<Integer>>) future -> {
            if (future.isSuccess()) {
                System.out.println("监听器1: 成功，结果为" + future.get());
            } else {
                System.out.println("监听器1: 失败，异常为" + future.cause());
            }
        });
        promise.addListener(new GenericFutureListener<Future<Integer>>() {
            @Override
            public void operationComplete(Future future) throws Exception {
                System.out.println("监听器2: 操作完成");
            }
        });

        eventExecutors.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                // promise.setSuccess("123");
                promise.setFailure(new RuntimeException("失败"));
            }
        });
        // sync 会抛出异常
        //promise.sync();
        // await 不会抛出异常
        promise.await();
    }
}
