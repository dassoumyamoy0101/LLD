public class LLD_1ToNUsingMThreads {
    public static void main(String[] args) {
        Shared shared = new Shared(1);
        for(int i=0; i<5; ++i) {
            Thread t = new Thread(new Task(shared, i, 30, 5));
            t.start();
        }
    }
}

class Task implements Runnable {
    final Shared shared;
    int rem, limit, m;

    Task(Shared shared, int rem, int limit, int m) {
        this.shared = shared;
        this.rem = rem;
        this.limit = limit;
        this.m = m;
    }

    @Override
    public void run() {
        while(shared.val <= limit) {
            synchronized (shared) {
                while(shared.val % m != rem) {
                    try {
                        shared.wait();
                    } catch (InterruptedException e) {
                    }
                }
                if(shared.val <= limit) {
                    System.out.println(shared.val + " from " + Thread.currentThread().getName());
                }
                shared.val++;
                shared.notifyAll();
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

/*

Print 1 to n using m threads IN ORDER

*/