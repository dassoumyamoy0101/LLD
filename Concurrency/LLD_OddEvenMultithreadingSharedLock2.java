public class LLD_OddEvenMultithreadingSharedLock2 {
    public static void main(String[] args) {
        Shared shared = new Shared(1);
        Thread odd = new Thread(new OddEvenThread(shared, 10, 1));
        Thread even = new Thread(new OddEvenThread(shared, 10, 0));
        odd.start();
        even.start();
    }
}

class OddEvenThread implements Runnable {
    int limit, rem;
    final Shared shared;

    public OddEvenThread(Shared shared, Integer limit, Integer rem) {
        this.shared = shared;
        this.limit = limit;
        this.rem = rem;
    }

    @Override
    public void run() {
        while(shared.val < limit) {
            synchronized (shared) {
                while(shared.val%2 != rem) {
                    try {
                        shared.wait();
                    } catch (InterruptedException e) {
                    }
                }
                System.out.println(shared.val + " from " + Thread.currentThread().getName());
                shared.val++;
                shared.notify();
            }
        }
    }
}

class Shared {
    int val;

    Shared(int val) {
        this.val = val;
    }
}