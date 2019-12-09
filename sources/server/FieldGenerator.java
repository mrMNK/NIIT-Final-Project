package server;

import java.util.Random;

public class FieldGenerator {

    public int generateLine(){
        Random random = new Random();
        int num = random.nextInt(4);
        return num;
    }
}
