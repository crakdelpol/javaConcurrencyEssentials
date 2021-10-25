import java.util.Random;

class JoinExample implements Runnable {
    private Random random = new Random(System.currentTimeMillis());

    public static void main(String[] args) throws InterruptedException {
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new JoinExample(), "Joining Thread " + i);
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("[" + Thread.currentThread().getName() + "] All Threads have finished");
    }

    @Override
    public void run() {
        for (int i = 0; i < 10000000; i++) {
            random.nextInt();
        }
        System.out.println("[" + Thread.currentThread().getName() + "] Finished");
    }
}
