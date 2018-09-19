import java.util.ArrayList;
import java.util.List;

public class Stack {

	private List<String> stack;
	
	public Stack() {
		stack = new ArrayList<>();
	}
	
	public void push(String element) {
		stack.add(element);
	}
	
	public String peek() {
		return stack.get(stack.size() - 1);
	}
	
	public String pop() {
		return stack.remove(stack.size() - 1);
	}
	
	public int size() {
		return stack.size();
	}
}
