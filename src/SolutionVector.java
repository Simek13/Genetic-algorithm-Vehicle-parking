import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SolutionVector implements Iterable<Integer> {

	public List<List<Integer>> solution;
	public double loss;
	public double fitness;

	public SolutionVector(int nrOfLines) {
		solution = new ArrayList<>();
		for (int i = 0; i < nrOfLines; i++) {
			solution.add(new ArrayList<>());
		}
	}

	public SolutionVector copyOf() {
		SolutionVector s = new SolutionVector(solution.size());
		for (int i = 0; i < solution.size(); i++) {
			List<Integer> line = solution.get(i);
			for (int j = 0; j < line.size(); j++) {
				s.solution.get(i).add(line.get(j));
			}
		}
		return s;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (List<Integer> line : solution) {
			for (int vehicle : line) {
				sb.append((vehicle + 1) + " ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	@Override
	public Iterator<Integer> iterator() {
		return new SolutionVectorIterator();
	}

	private class SolutionVectorIterator implements Iterator<Integer> {

		private int line;
		private int position;

		public SolutionVectorIterator() {
			line = 0;
			position = 0;
		}

		@Override
		public boolean hasNext() {
			return solution.size() <= line;
		}

		@Override
		public Integer next() {
			position++;
			if (solution.get(line).size() < position) {
				line++;
				position = 0;
			}
			return solution.get(line).get(position - 1);
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((solution == null) ? 0 : solution.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SolutionVector other = (SolutionVector) obj;
		if (solution == null) {
			if (other.solution != null)
				return false;
		} else if (!solution.equals(other.solution))
			return false;
		return true;
	}

}
