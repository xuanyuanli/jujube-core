package org.jujubeframework;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

public class CountTest {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        //创建一个计算任务，计算 由1加到12
        CountTask countTask = new CountTask(1, 12);
        Future<Integer> future = forkJoinPool.submit(countTask);
        System.out.println("最终的计算结果：" + future.get());
    }
}

class CountTask extends RecursiveTask<Integer> {

    private static final int THRESHOLD = 2;
    private final int start;
    private final int end;


    public CountTask(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer compute() {
        int sum = 0;
        boolean canCompute = (end - start) <= THRESHOLD;

        //任务已经足够小，可以直接计算，并返回结果
        if (canCompute) {
            for (int i = start; i <= end; i++) {
                sum += i;
            }
            System.out.println("执行计算任务，计算    " + start + "到 " + end + "的和  ，结果是：" + sum + "   执行此任务的线程：" + Thread.currentThread().getName());
            return sum;
        } else { //任务过大，需要切割
            System.out.println("任务过大，切割的任务：  " + start + "加到 " + end + "的和       执行此任务的线程：" + Thread.currentThread().getName());
            int middle = (start + end) / 2;
            //切割成两个子任务
            CountTask leftTask = new CountTask(start, middle);
            CountTask rightTask = new CountTask(middle + 1, end);
            //执行子任务
            leftTask.fork();
            rightTask.fork();
            //等待子任务的完成，并获取执行结果
            invokeAll(leftTask,rightTask);
//            int leftResult = leftTask.join();
//            int rightResult = rightTask.join();
            //合并子任务
//            sum = leftResult + rightResult;
//            return sum;
            return leftTask.join()+rightTask.join();
        }

    }
}

