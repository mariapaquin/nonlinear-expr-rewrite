public class If {
    public void m(int a, int b, int c){
        a = b*c;

        if (true) {
            b = b+1;
        }

        c = c+1;
        a = b*c;
        System.out.println(b*c);
    }
}


//public class If {
//    public void m(int a, int b, int c){
//        a = b*c;
//
//        if (true) {
//            b = b+1;
//        }
//
//        c = c+1;
//        a = b*c;
//        System.out.println(b*c);
//    }
//}