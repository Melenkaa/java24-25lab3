import java.util.concurrent.Semaphore;

public class RollerCoaster {
    private static final int M = 5;

    private static Semaphore turnstile = new Semaphore(1);
    private static Semaphore controller = new Semaphore(0);
    private static Semaphore cart = new Semaphore(0);

    private static int passengersOnPlatform = 0;

    public static void main(String[] args) throws InterruptedException {
        Thread turnstileThread = new Thread(new Turnstile());
        Thread controllerThread = new Thread(new Controller());
        Thread cartThread = new Thread(new Cart());

        turnstileThread.start();
        controllerThread.start();
        cartThread.start();

        Thread.sleep(6000);

        turnstileThread.interrupt();
        controllerThread.interrupt();
        cartThread.interrupt();
    }

    static class Turnstile implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    turnstile.acquire();
                    synchronized (this) {
                        passengersOnPlatform++;
                        System.out.println("The passenger arrived on the platform. Passengers: " + passengersOnPlatform);
                        if (passengersOnPlatform == M) {
                            controller.release();
                        } else {
                            turnstile.release();
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Controller implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    controller.acquire();
                    System.out.println("The controller signals the departure of the cart.");
                    cart.release();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Cart implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    cart.acquire();
                    System.out.println("The cart is leaving.");
                    Thread.sleep(2000);

                    synchronized (this) {
                        passengersOnPlatform = 0;
                        System.out.println("The cart returned. The passengers left the platform.");
                        turnstile.release();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
