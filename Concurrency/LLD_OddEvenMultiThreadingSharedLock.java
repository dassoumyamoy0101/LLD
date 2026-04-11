public class LLD_OddEvenMultiThreadingSharedLock {
    public static void main(String[] args) {
        State state = new State();
        Thread t1 = new Thread(new OddEvenThread(1, 10, state));
        Thread t2 = new Thread(new OddEvenThread(2, 10, state));
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
        }
    }
}

class OddEvenThread implements Runnable {
    int val, limit;
    State state;

    public OddEvenThread(int val, int limit, State state) {
        this.val = val;
        this.limit = limit;
        this.state = state;
    }   

    @Override
    public void run() {
        while(val <= limit) {
            // state is a shared lock across all the threads
            // hence, although multiple thread objects will have separate instances, they would be locked on the same resource
            synchronized (state) {
                while(((val & 1) == 1 && this.state.getExpectedState() == Type.EVEN) || ((val & 1) == 0 && this.state.getExpectedState() == Type.ODD)) {
                    try {
                        this.state.wait();
                    } catch (InterruptedException e) {
                    }
                }
                System.out.println(val + " from " + Thread.currentThread().getName());
                val += 2;
                this.state.inverseExpectedState();
                this.state.notify();
            }
        }
    }
}

class State {
    private Type expectedState;

    State() {
        expectedState = Type.ODD;
    }

    public Type getExpectedState() {
        return expectedState;
    }

    public void inverseExpectedState() {
        if(expectedState == Type.EVEN) {
            expectedState = Type.ODD;
        }
        else {
            expectedState = Type.EVEN;
        }
    }
}

enum Type {
    ODD, EVEN
}