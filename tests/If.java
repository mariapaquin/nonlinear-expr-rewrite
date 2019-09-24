public class If {

    public void m(int a, int b){
        b = 2;

        if (true) {
            b = 1;
        }
        a = b;
        b = 1;
        a = 1;
    }
}
