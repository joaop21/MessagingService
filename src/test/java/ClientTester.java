import Middleware.MiddlewareFacade;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ClientTester extends Thread {
    private static MiddlewareFacade mfac;

    public ClientTester(MiddlewareFacade mfa){
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

        new Thread(new ClientTester(mfac)).start();

        BufferedReader sin = new BufferedReader(new InputStreamReader(System.in));

        try {
            String current;
            while ((current = sin.readLine()) != null) {
                //mfac.sendClientMessage(current);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
