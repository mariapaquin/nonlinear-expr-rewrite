public class If {
    public void m(int a, int b, int c){
        int x0 = Debug.makeSymbolicInteger("x0");
		a = x0;

        if (true) {
            b = b+1;
			x0 = Debug.makeSymbolicInteger("x1");
        }
        c = c+1;
		x0 = Debug.makeSymbolicInteger("x2");
        a = x0;
    }
}
