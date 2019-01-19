import java.util.ArrayList;
import java.util.List;

public class Line {

	public double length;
	public List<Integer> blocks = new ArrayList<>();

	public Line() {
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Length: " + length + "\n");
		sb.append("Blocks: ");
		for (int b : blocks) {
			sb.append((b + 1) + " ");
		}
		sb.append("\n");
		return sb.toString();
	}

}
