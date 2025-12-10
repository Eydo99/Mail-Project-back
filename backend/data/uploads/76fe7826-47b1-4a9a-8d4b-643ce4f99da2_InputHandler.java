
import java.util.Scanner;

public class InputHandler {
    
    private Scanner scanner;
    
    public InputHandler() {
        this.scanner = new Scanner(System.in);
    }
    
    public int readPositiveInteger(String prompt) {
        int number;
        
        while (true) {
            System.out.print(prompt);
            
            if (!scanner.hasNextInt()) {
                System.out.println("[X] Error: Please enter a valid integer!");
                scanner.next();
                continue;
            }
            
            number = scanner.nextInt();
            
            if (number <= 0) {
                System.out.println("[X] Error: Number must be a positive integer!");
                continue;
            }
            
            break;
        }
        
        return number;
    }
    
    public int readIntegerInRange(String prompt, int min, int max) {
        int number;
        
        while (true) {
            System.out.print(prompt);
            
            if (!scanner.hasNextInt()) {
                System.out.println("[X] Error: Please enter a valid integer!");
                scanner.next();
                continue;
            }
            
            number = scanner.nextInt();
            
            if (number < min || number > max) {
                System.out.println("[X] Error: Number must be between " + min + " and " + max + "!");
                continue;
            }
            
            break;
        }
        
        return number;
    }
    
    public int[] readTwoPositiveIntegers() {
        int[] numbers = new int[2];
        
        System.out.println("\nEnter two positive integers:");
        numbers[0] = readPositiveInteger("  First number:  ");
        numbers[1] = readPositiveInteger("  Second number: ");
        
        return numbers;
    }
    
    public boolean readYesNo(String prompt) {
        String response;
        
        while (true) {
            System.out.print(prompt + " (y/n): ");
            response = scanner.next().trim().toLowerCase();
            
            if (response.equals("y") || response.equals("yes")) {
                return true;
            } else if (response.equals("n") || response.equals("no")) {
                return false;
            } else {
                System.out.println("[X] Error: Please enter 'y' or 'n'!");
            }
        }
    }
    
    public int readMenuChoice(int maxOption) {
        return readIntegerInRange("Enter your choice: ", 1, maxOption);
    }
    
    public void pressEnterToContinue() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
        scanner.nextLine();
    }
    
    public void clearBuffer() {
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
    }
    
    public void close() {
        scanner.close();
    }
    
    public static boolean isPositiveInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        
        try {
            int num = Integer.parseInt(str);
            return num > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static boolean isWithinSafeRange(int num) {
        return num > 0 && num <= 1_000_000_000;
    }
    
    public int readSafeInteger(String prompt) {
        int number;
        
        while (true) {
            number = readPositiveInteger(prompt);
            
            if (!isWithinSafeRange(number)) {
                System.out.println("[!] Warning: Number too large! Please enter a smaller number.");
                continue;
            }
            
            break;
        }
        
        return number;
    }
}