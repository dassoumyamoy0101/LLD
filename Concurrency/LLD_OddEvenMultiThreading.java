public class LLD_OddEvenMultiThreading {
    public static void main(String[] args) {
        Printer printer = new Printer(1, 10);
        Thread oddThread = new Thread(new OddEvenThread(printer), "OddThread");
        Thread evenThread = new Thread(new OddEvenThread(printer), "EvenThread");
        oddThread.start();
        evenThread.start();
        try {
            oddThread.join();
            evenThread.join();
        } catch (InterruptedException e) {
        }
    }
}

class OddEvenThread implements Runnable {
    Printer printer;

    public OddEvenThread(Printer printer) {
        this.printer = printer;
    }

    @Override
    public void run() {
        this.printer.print();
    }
}

class Printer {
    int val, limit;

    Printer(int val, int limit) {
        this.val = val;
        this.limit = limit;
    }

    public void print() {
        while(val < limit) {
            synchronized (this) {
                while((Thread.currentThread().getName().equals("OddThread") && ((val & 1) == 0)) || (Thread.currentThread().getName().equals("EvenThread") && ((val & 1) == 1))) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                    }
                }
                System.out.println(val + " from " + Thread.currentThread().getName());
                val++;
                this.notify();
            }
        }
    }
}
