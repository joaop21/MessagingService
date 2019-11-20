package Server;

import Middleware.MiddlewareFacade;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * This class is for testing the current code.
 * In the future it will be deleted.
 * */
public class MainTester extends Thread{
    private static MiddlewareFacade mfac;

    public MainTester(MiddlewareFacade mfa){
        mfac = mfa;
    }

    @Override
    public void run() {
        while(true){
            /*
            try {
                String line = (String) mfac.getMessage();
                System.out.println(line);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);

        mfac = new MiddlewareFacade(port);

        new Thread(new MainTester(mfac)).start();

        BufferedReader sin = new BufferedReader(new InputStreamReader(System.in));

        try {
            String current;
            while ((current = sin.readLine()) != null) {
                mfac.sendServerMessage(current);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
