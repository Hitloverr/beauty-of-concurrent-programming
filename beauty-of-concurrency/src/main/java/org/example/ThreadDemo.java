package org.example;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ThreadDemo {
    public static void main(String[] args) throws Exception{
        Callable<Object> callable = () -> 1;
        FutureTask<Object> futureTask = new FutureTask<>(callable);
        new Thread(futureTask).start();
        futureTask.get();
    }

    volatile boolean running = false;
    // wait使用
    private void waitDemo() throws InterruptedException {
        synchronized (this) {
            while (!running) { // 虚假唤醒，等待条件满足
                wait();
            }
        }

        Queue<Object> queue = new LinkedList<>();
        // 生产者
        synchronized (queue) {
            while (queue.size() == Integer.MAX_VALUE) { // 虚假唤醒，等待条件满足
                queue.wait();
            }
            queue.add(new Object());
            queue.notifyAll();
        }

        // 消费者
        synchronized (queue) {
            while (queue.isEmpty()) { // 虚假唤醒，等待条件满足
                queue.wait();
            }
            queue.poll();
            queue.notifyAll();
        }
    }

    // 中断唤醒
    private void interruptDemo() throws InterruptedException {
        Runnable runnable = () -> {
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        Thread.sleep(1000);
        thread.interrupt();
    }

    private void joinDemo() throws InterruptedException {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
        thread.join();
        System.out.println("main end");
    }
}
