package Server;

public class ServerApplication extends Thread{
    private OperationsHandler opshandler;

    private ServerApplication(int server_port){
        MiddlewareFacade midd = new MiddlewareFacade(server_port);
        this.opshandler = new OperationsHandler(midd);
    }

    @Override
    public void run() {
        this.opshandler.receiveOperations();

        // For now this serves, it's a little trick for the program not to die
        synchronized (this) {
            while (true) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        int server_port = Integer.parseInt(args[0]);

        new Thread(new ServerApplication(server_port)).run();
    }
}
