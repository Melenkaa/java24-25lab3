import java.util.concurrent.Semaphore;

public class RollerCoaster {
    private static final int M = 5;

    private static Semaphore turnstile = new Semaphore(1);
    private static Semaphore controller = new Semaphore(0);
    private static Semaphore cart = new Semaphore(0);

    private static final Object lock = new Object();

    private static int passengersOnPlatform = 0;

    public static void main(String[] args) throws InterruptedException {
        Thread turnstileThread = new Thread(new Turnstile(), "TurnstileThread");
        Thread controllerThread = new Thread(new Controller(), "ControllerThread");
        Thread cartThread = new Thread(new Cart(), "CartThread");

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
                    synchronized (lock) {
                        passengersOnPlatform++;
                        System.out.println(Thread.currentThread().getName() +
                                ": The passenger arrived on the platform. Total passengers: " + passengersOnPlatform);
                        if (passengersOnPlatform == M) {
                            System.out.println(Thread.currentThread().getName() +
                                    ": Reached capacity. Signaling controller.");
                            controller.release();
                        } else {
                            System.out.println(Thread.currentThread().getName() +
                                    ": Waiting for more passengers.");
                            turnstile.release();
                        }
                    }
                }
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + ": Interrupted. Exiting.");
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
                    System.out.println(Thread.currentThread().getName() +
                            ": The controller signals the departure of the cart.");
                    cart.release();
                }
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + ": Interrupted. Exiting.");
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
                    System.out.println(Thread.currentThread().getName() +
                            ": The cart is leaving with full load.");
                    Thread.sleep(2000);

                    synchronized (lock) {
                        passengersOnPlatform = 0;
                        System.out.println(Thread.currentThread().getName() +
                                ": The cart returned. Passengers left the platform.");
                        turnstile.release();
                    }
                }
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + ": Interrupted. Exiting.");
                Thread.currentThread().interrupt();
            }
        }
    }
}
