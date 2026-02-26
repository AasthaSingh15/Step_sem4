import java.util.*;

interface PalindromeStrategy {
    boolean isPalindrome(String str);
}

class StackStrategy implements PalindromeStrategy {
    public boolean isPalindrome(String str) {
        Stack<Character> stack = new Stack<>();
        for (char c : str.toCharArray())
            stack.push(c);

        String reversed = "";
        while (!stack.isEmpty())
            reversed += stack.pop();

        return str.equals(reversed);
    }
}

class DequeStrategy implements PalindromeStrategy {
    public boolean isPalindrome(String str) {
        Deque<Character> deque = new LinkedList<>();
        for (char c : str.toCharArray())
            deque.addLast(c);

        while (deque.size() > 1) {
            if (!deque.removeFirst().equals(deque.removeLast()))
                return false;
        }
        return true;
    }
}

class PalindromeService {
    private PalindromeStrategy strategy;

    public void setStrategy(PalindromeStrategy strategy) {
        this.strategy = strategy;
    }

    public boolean execute(String str) {
        return strategy.isPalindrome(str);
    }

    public boolean basicCheck(String str) {
        int left = 0;
        int right = str.length() - 1;

        while (left < right) {
            if (str.charAt(left) != str.charAt(right))
                return false;
            left++;
            right--;
        }
        return true;
    }
}

public class PalindromeCheckerAPP {

    public static void useCase1_Welcome() {
        System.out.println("Welcome to the Palindrome Checker Management System");
    }

    public static void useCase2_HalfLoop(String str) {
        boolean result = true;
        for (int i = 0; i < str.length() / 2; i++)
            if (str.charAt(i) != str.charAt(str.length() - 1 - i))
                result = false;
        System.out.println("Half Loop: " + result);
    }

    public static void useCase3_BackwardCheck(String str) {
        String reversed = "";
        for (int i = str.length() - 1; i >= 0; i--)
            reversed += str.charAt(i);
        System.out.println("Backward Method: " + str.equals(reversed));
    }

    public static void useCase4_CharacterArray(String str) {
        char[] arr = str.toCharArray();
        int left = 0, right = arr.length - 1;
        boolean result = true;
        while (left < right) {
            if (arr[left] != arr[right])
                result = false;
            left++;
            right--;
        }
        System.out.println("Character Array: " + result);
    }

    public static void useCase5_StackBased(String str) {
        Stack<Character> stack = new Stack<>();
        for (char c : str.toCharArray())
            stack.push(c);

        String reversed = "";
        while (!stack.isEmpty())
            reversed += stack.pop();

        System.out.println("Stack Based: " + str.equals(reversed));
    }

    public static void useCase6_QueueStack(String str) {
        Stack<Character> stack = new Stack<>();
        Queue<Character> queue = new LinkedList<>();

        for (char c : str.toCharArray()) {
            stack.push(c);
            queue.add(c);
        }

        boolean result = true;
        while (!stack.isEmpty()) {
            if (!stack.pop().equals(queue.remove()))
                result = false;
        }

        System.out.println("Queue + Stack: " + result);
    }

    public static void useCase7_DequeBased(String str) {
        Deque<Character> deque = new LinkedList<>();
        for (char c : str.toCharArray())
            deque.addLast(c);

        boolean result = true;
        while (deque.size() > 1) {
            if (!deque.removeFirst().equals(deque.removeLast()))
                result = false;
        }

        System.out.println("Deque Based: " + result);
    }

    public static void useCase8_LinkedListBased(String str) {
        LinkedList<Character> list = new LinkedList<>();
        for (char c : str.toCharArray())
            list.add(c);

        boolean result = true;
        while (list.size() > 1) {
            if (!list.removeFirst().equals(list.removeLast()))
                result = false;
        }

        System.out.println("LinkedList Based: " + result);
    }

    public static boolean recursiveCheck(String str, int start, int end) {
        if (start >= end)
            return true;
        if (str.charAt(start) != str.charAt(end))
            return false;
        return recursiveCheck(str, start + 1, end - 1);
    }

    public static void useCase9_Recursive(String str) {
        System.out.println("Recursive: " + recursiveCheck(str, 0, str.length() - 1));
    }

    public static void useCase10_CaseInsensitiveSpaceIgnored(String str) {
        String cleaned = str.replaceAll("\\s+", "").toLowerCase();
        System.out.println("Case-Insensitive & Space-Ignored: "
                + recursiveCheck(cleaned, 0, cleaned.length() - 1));
    }

    public static void useCase11_ObjectOrientedService(String str) {
        PalindromeService service = new PalindromeService();
        System.out.println("Object-Oriented Service: " + service.basicCheck(str));
    }

    public static void useCase12_StrategyPattern(String str) {
        PalindromeService service = new PalindromeService();

        service.setStrategy(new StackStrategy());
        System.out.println("Strategy (Stack): " + service.execute(str));

        service.setStrategy(new DequeStrategy());
        System.out.println("Strategy (Deque): " + service.execute(str));
    }

    public static void useCase13_PerformanceComparison(String str) {

        long start, end;

        start = System.nanoTime();
        boolean r1 = recursiveCheck(str, 0, str.length() - 1);
        end = System.nanoTime();
        System.out.println("Recursive Result: " + r1 + " | Time: " + (end - start) + " ns");

        start = System.nanoTime();
        StackStrategy s = new StackStrategy();
        boolean r2 = s.isPalindrome(str);
        end = System.nanoTime();
        System.out.println("Stack Result: " + r2 + " | Time: " + (end - start) + " ns");

        start = System.nanoTime();
        DequeStrategy d = new DequeStrategy();
        boolean r3 = d.isPalindrome(str);
        end = System.nanoTime();
        System.out.println("Deque Result: " + r3 + " | Time: " + (end - start) + " ns");
    }

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        useCase1_Welcome();

        System.out.println("Enter a string:");
        String input = sc.nextLine();

        useCase2_HalfLoop(input);
        useCase3_BackwardCheck(input);
        useCase4_CharacterArray(input);
        useCase5_StackBased(input);
        useCase6_QueueStack(input);
        useCase7_DequeBased(input);
        useCase8_LinkedListBased(input);
        useCase9_Recursive(input);
        useCase10_CaseInsensitiveSpaceIgnored(input);
        useCase11_ObjectOrientedService(input);
        useCase12_StrategyPattern(input);
        useCase13_PerformanceComparison(input);

        sc.close();
    }
}