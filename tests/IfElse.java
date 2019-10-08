public class IfElse {
    public void m(int a, int b, int c){
        int x0 = Debug.makeSymbolicInteger("x0");
		a = x0;

        if (true) {
            b = b+1;
        } else {
        }

        c = c+1;
		x0 = Debug.makeSymbolicInteger("x0");
        a = x0;
        System.out.println(x0);
    }
}


//public class IfElse {
//    public void m(int a, int b, int c){
//        a = b*c;
//
//        if (true) {
//            b = b+1;
//        } else {
//            c = c+1;
//        }
//
//        c = c+1;
//        a = b*c;
//        System.out.println(b*c);
//    }
//}