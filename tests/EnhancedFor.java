import java.util.HashSet;

public class EnhancedFor {
    HashSet<String> strings = new HashSet<>();

    public void m(int a, int b, int c) {
        a = 1;
        b = 1;
        for (String str: strings) {
           a = 2;
        }
        System.out.println(c);
    }
}
