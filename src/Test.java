import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Test {

	public static void main(String[] args) throws IOException {

		Genetics g = new Genetics("instanca3.txt");
		SolutionVector best = g.generationGeneticAlgorithm(true, 500, 0.5, 400);
		// SolutionVector best = g.eliminativeGeneticAlgorithm(100, 0.8, 10000);

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter("rjesenje3.txt"));
		} catch (IOException e) {
			System.err.println("Cannot open given file.");
		}
		bw.write(best.toString());
		bw.close();
		System.out.println(best);
		System.out.println("Constraints?" + g.checkConstraints(best));
		System.out.println(best.loss);
		System.out.println(best.fitness);
		System.out.println(best.fitness / best.loss);
	}
}
