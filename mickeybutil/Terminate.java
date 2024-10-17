package mickeybutil;

public class Terminate {
    public static void exception(Exception e) {
        System.err.println(e);
        e.printStackTrace();
        System.exit(0);
    }

    public static void assertEq(Object a, Object b) {
        if (!a.equals(b))
            throw new Error(a + " != " + b);
    }
}
