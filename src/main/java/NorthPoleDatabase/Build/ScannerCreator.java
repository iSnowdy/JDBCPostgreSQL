package NorthPoleDatabase.Build;

import java.util.Scanner;

// Utility class to create and use Scanners
public class ScannerCreator {
    // Scanner Object declared as final as a protection method
    private static final Scanner scanner = new Scanner(System.in);

    // We only want this class to be able to create and utilize methods from Scanner, hence why private
    private ScannerCreator() {}

    // All methods are public and static so we can access them from this single class
    // across all the package
    public static Scanner getScanner() {
        return scanner;
    }
    public static void closeScanner() {
        scanner.close();
    }
    // Like this we avoid those pesky Strings into Int Scanners
    // that will cause InputMissMatch Exceptions
    public static int nextInt() {
        while (true) {
            if (scanner.hasNextInt()) {
                return scanner.nextInt();
            } else {
                System.out.println("Invalid input. It must be an integer. Please try again");
                scanner.nextLine();
            }
        }
    }
    public static double nextFloat() {
        return scanner.nextFloat();
    }
    public static String nextLine() {
        return scanner.nextLine();
    }
    public static String next() {
        return scanner.next();
    }
}
