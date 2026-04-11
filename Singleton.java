public class Singleton {
    public static void main(String[] args) {
        System.out.println("Inside main method");
        Mango mango = Mango.getInstance();
        mango.printVal();
    }
}

// final is to prevent inheritance
final class Mango {
    private static Mango obj;

    // private constructor is to prevent instantiation
    private Mango() {
    }

    public static Mango getInstance() {
        if(obj == null) {
            synchronized (Mango.class) {
                obj = new Mango();
            }
        }
        return obj;
    }

    // utility methods in the singleton class
    public void printVal() {
        System.out.println("Inside " + Mango.obj.getClass());
    }
}
