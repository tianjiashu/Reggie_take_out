import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class test {
    public static void main(String[] args) {
        /*
        并发修改异常
         */
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        for (String s : list) {
            if(s.equals("1"))list.remove(s);
        }
        System.out.println(list);
    }
}
