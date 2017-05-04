import net.fbridault.terminal_command.Command;
import net.fbridault.terminal_command.Console;

public class Main extends Console{

    public static void main(String[] args) {
        Console c = new Main();

        while (true) {
            c.executeNext();
        }
    }


    @Command(name = "println")
    public void print(String s) {
        System.out.println(s);
    }

    @Command(parameters = {"number_1", "number_2"})
    public void add(int a, int b) {
        System.out.println(a + b);
    }

    @Command
    public void sub(float a, float b) {
        System.out.println(a - b);
    }
}
