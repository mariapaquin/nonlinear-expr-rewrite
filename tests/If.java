public class If {
    public void m(int a, int b, int c){
        int x0 = Debug.makeSymbolicInteger("x0");
		a = x0;

        if (true) {
            a = x0;
            b = b+1;
			x0 = Debug.makeSymbolicInteger("x0");
        }

        c = c+1;
		x0 = Debug.makeSymbolicInteger("x0");
        a = x0;
    }
}

//public class If {
//    public void m(int a, int b, int c){
//        a = b*c;
//
//        if (true) {
//            a = b*c;
//            b = b+1;
//        }
//
//        c = c+1;
//        a = b*c;
//    }
//}
