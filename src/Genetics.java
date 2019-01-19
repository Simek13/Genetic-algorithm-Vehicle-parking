import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Genetics {

	public DataLoader dl;
	public double p1;
	public double p2;
	public double p3;
	public double r2;

	public Genetics(String file) {
		dl = new DataLoader(file);
		try {
			dl.load();
		} catch (IOException e) {
			System.out.println("Pogreška kod čitanja datoteke.");
			System.exit(-1);
		}

		p1 = 1. / (dl.nrOfLines - 1);
		p2 = 1. / dl.nrOfLines;
		p3 = 1. / (lineCapacity() - allVehiclesLength());
		r2 = 1. / (dl.nrOfLines - 1);

	}

	public SolutionVector generationGeneticAlgorithm(boolean elitism, int popSize, double mutationProb,
			int generations) {
		SolutionVector[] population = generatePopulation(popSize);
		SolutionVector lastBest = findBest(population);
		SolutionVector bestSolution = lastBest;
		System.out.println("Početno najbolje rješenje: Loss: " + bestSolution.loss + ", Fitness: "
				+ bestSolution.fitness + ", Omjer: " + bestSolution.fitness / bestSolution.loss);

		for (int i = 0; i < generations; i++) {

			if (i % 100 == 0) {
				System.out.println("Generacija: " + i);
			}

			SolutionVector[] nextPopulation = new SolutionVector[popSize];
			if (elitism) {
				nextPopulation[0] = findBest(population);
			} else {
				nextPopulation[0] = evolve(population, 2, mutationProb);
			}

			for (int j = 1; j < popSize; j++) {
				nextPopulation[j] = evolve(population, 2, mutationProb);
			}
			population = nextPopulation;
			bestSolution = findBest(population);
			if (bestSolution.fitness / bestSolution.loss > lastBest.fitness / lastBest.loss) {
				System.out.println("Novo najbolje rješenje; Trenutna generacija: " + (i + 1) + " ; Kazna: "
						+ bestSolution.loss + ", Fitness: " + bestSolution.fitness + ", Omjer: "
						+ bestSolution.fitness / bestSolution.loss);
			}
			lastBest = bestSolution;
		}
		return bestSolution;

	}

	public SolutionVector eliminativeGeneticAlgorithm(int popSize, double mutationProb, int generations)
			throws IOException {

		SolutionVector[] population = generatePopulation(popSize);
		SolutionVector lastBest = findBest(population);
		SolutionVector bestSolution = lastBest;
		SolutionVector child = null, worst = null;
		int j, k;

		boolean prvi = false, drugi = false;
		System.out.println("Početno najbolje rješenje: Loss: " + bestSolution.loss + ", Fitness: "
				+ bestSolution.fitness + ", Omjer: " + bestSolution.fitness / bestSolution.loss);
		long startTime = System.currentTimeMillis();

		for (int i = 0; i < generations; i++) {
			// long estimatedTime = System.currentTimeMillis() - startTime;
			// if (estimatedTime / 1000 > 60 && !prvi) {
			// System.out.println("Prvo rjesenje");
			// writeToFile("rjesenjePrvaMinuta.txt", lastBest, i);
			// prvi = true;
			// }
			// if (estimatedTime / 1000 > 300 && !drugi) {
			// System.out.println("Drugo rjesenje");
			// writeToFile("rjesenjePetaMinuta.txt", lastBest, i);
			// drugi = true;
			// }

			do {
				SolutionVector[] parents = rouletteWheelSelection(population, 3);
				SolutionVector[] betterParents = new SolutionVector[2];
				worst = parents[0];
				double minRatio = parents[0].fitness / parents[0].loss;
				if (parents[1].fitness / parents[1].loss < minRatio) {
					worst = parents[1];
					minRatio = parents[1].fitness / parents[1].loss;
				}
				if (parents[2].fitness / parents[2].loss < minRatio) {
					worst = parents[2];
				}
				j = 0;
				k = 0;
				while (k < 2) {
					if (!parents[j].equals(worst)) {
						betterParents[k] = parents[j];
						k++;
					}
					j++;
				}

				child = crossover(betterParents[0], betterParents[1]);
				if (child != null) {
					child = mutate(child, mutationProb);
				}
			} while (child == null || Arrays.asList(population).contains(child));

			child.loss = firstGoal(child);
			child.fitness = secondGoal(child);
			for (j = 0; j < popSize; j++) {
				if (population[j].equals(worst)) {
					population[j] = child;
					break;
				}
			}

			bestSolution = findBest(population);
			if (bestSolution.fitness / bestSolution.loss > lastBest.fitness / lastBest.loss) {
				System.out.println("Novo najbolje rješenje; Trenutna generacija: " + (i + 1) + " ; Kazna: "
						+ bestSolution.loss + ", Fitness: " + bestSolution.fitness + ", Omjer: "
						+ bestSolution.fitness / bestSolution.loss);
			}
			lastBest = bestSolution;
		}
		return bestSolution;

	}

	public void writeToFile(String fileName, SolutionVector best, int iterations) throws IOException {
		StringBuilder sb = new StringBuilder();
		if (iterations > 0) {
			sb.append("Broj evaluacija: " + iterations + "\n");
		}
		sb.append("Prva funkcija cilja: " + firstGoal(best) + "\n");
		sb.append("Druga funkcija cilja: " + secondGoal(best) + "\n");
		sb.append(best.toString());
		BufferedWriter bw = new BufferedWriter(new FileWriter("rjesenjePetaMinuta.txt"));
		bw.write(sb.toString());
		bw.close();
	}

	private SolutionVector evolve(SolutionVector[] population, int numOfUnits, double mutationProb) {
		SolutionVector child = null;
		do {
			SolutionVector[] parents = rouletteWheelSelection(population, numOfUnits);
			child = crossover(parents[0], parents[1]);
			if (child != null) {
				child = mutate(child, mutationProb);
			}
		} while (child == null || Arrays.asList(population).contains(child));
		child.loss = firstGoal(child);
		child.fitness = secondGoal(child);
		return child;
	}

	private SolutionVector[] rouletteWheelSelection(SolutionVector[] population, int numOfUnits) {
		SolutionVector[] parents = new SolutionVector[numOfUnits];
		double worstRatio = findWorstRatio(population);
		double balance = 0.05;
		if (worstRatio < 0) {
			balance += -worstRatio;
		}
		double sumOfRatios = 0;
		Random rand = new Random();
		for (int i = 0; i < population.length; i++) {
			sumOfRatios += population[i].fitness / population[i].loss + balance;
		}
		loop: for (int i = 0; i < numOfUnits; i++) {
			double chosen = 0;
			double r = rand.nextDouble();
			for (int j = 0; j < population.length; j++) {
				chosen += (population[j].fitness / population[j].loss + balance) / sumOfRatios;
				if (r < chosen) {

					for (int k = 0; k < i; k++) {
						if (parents[k].equals(population[j])) {
							i--;
							continue loop;
						}
					}

					parents[i] = population[j];
					break;
				}
			}
		}
		return parents;
	}

	private double findWorstRatio(SolutionVector[] population) {
		double worstRatio = Double.POSITIVE_INFINITY;
		for (int i = 0; i < population.length; i++) {
			if (population[i].fitness / population[i].loss < worstRatio) {
				worstRatio = population[i].fitness / population[i].loss;
			}
		}
		return worstRatio;
	}

	public SolutionVector findBest(SolutionVector[] population) {
		SolutionVector best = null;
		double bestRatio = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < population.length; i++) {
			if (population[i].fitness / population[i].loss > bestRatio) {
				best = population[i];
				bestRatio = population[i].fitness / population[i].loss;
			}
		}
		return best;
	}

	private SolutionVector mutate(SolutionVector unit, double prob) {
		Random rand = new Random();
		SolutionVector mutated = unit.copyOf();

		if (rand.nextDouble() < prob) {
			if (rand.nextDouble() > 0.5) {
				mutated = oneVehicleSwitchMutation(mutated);
			} else {
				mutated = vehicleSwitchMutation(mutated);
			}
		}

		return mutated;
	}

	private int getShortestVehicleNumber(List<Vehicle> vehicles) {
		double shortest = vehicles.get(0).length;
		int number = vehicles.get(0).number;
		for (int i = 1; i < vehicles.size(); i++) {
			if (shortest > vehicles.get(i).length) {
				shortest = vehicles.get(i).length;
				number = vehicles.get(i).number;
			}
		}
		return number;
	}

	private double lineCapacity() {
		double capacity = 0;
		for (Line line : dl.lines) {
			capacity += line.length;
		}
		return capacity;
	}

	private double allVehiclesLength() {
		double length = 0;
		for (Vehicle vehicle : dl.vehicles) {
			length += vehicle.length;
		}
		return length;
	}

	public boolean oneLineConstraint(SolutionVector solution) {
		int[] parkedVehicles = new int[dl.nrOfVehicles];

		for (List<Integer> line : solution.solution) {
			for (int vehicle : line) {
				if (parkedVehicles[vehicle] != 0) {
					return false;
				}
				parkedVehicles[vehicle]++;
			}
		}
		return true;
	}

	public boolean oneSeriesLineConstraint(SolutionVector solution) {
		int series;
		for (List<Integer> line : solution.solution) {
			if (!(line.isEmpty())) {
				series = dl.vehicles[line.get(0)].series;
				for (int i = 1; i < line.size(); i++) {
					if (dl.vehicles[line.get(i)].series != series) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public boolean infrastructureConstraint(SolutionVector solution) {
		List<Integer> line;
		for (int i = 0; i < solution.solution.size(); i++) {
			line = solution.solution.get(i);
			for (int vehicle : line) {
				if (!(dl.restrictions[vehicle][i])) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean vehicleLengthConstraint(SolutionVector solution) {
		List<Integer> line;
		double length;
		for (int i = 0; i < solution.solution.size(); i++) {
			line = solution.solution.get(i);
			if (!(line.isEmpty())) {
				length = dl.vehicles[line.get(0)].length;
				for (int j = 1; j < line.size(); j++) {
					length += dl.vehicles[line.get(j)].length + 0.5;
				}
				if (length > dl.lines[i].length) {
					return false;
				}
			}

		}
		return true;
	}

	public boolean timesConstraint(SolutionVector solution) {
		for (List<Integer> line : solution.solution) {
			for (int i = 0; i < line.size() - 1; i++) {
				if (dl.vehicles[line.get(i)].startTime > dl.vehicles[line.get(i + 1)].startTime) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean blockingTimesConstraint(SolutionVector solution) {
		List<Integer> line, bLine;
		for (int i = 0; i < solution.solution.size(); i++) {
			if (!(dl.lines[i].blocks.isEmpty())) {
				line = solution.solution.get(i);
				for (int blockedLine : dl.lines[i].blocks) {
					bLine = solution.solution.get(blockedLine);
					if (!(line.isEmpty()) && !(bLine.isEmpty())) {
						if (dl.vehicles[line.get(line.size() - 1)].startTime > dl.vehicles[bLine.get(0)].startTime) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	public boolean checkConstraints(SolutionVector solution) {
		return oneLineConstraint(solution) && oneSeriesLineConstraint(solution) && infrastructureConstraint(solution)
				&& vehicleLengthConstraint(solution) && timesConstraint(solution) && blockingTimesConstraint(solution);
	}

	public int f1(SolutionVector solution) {
		int f = 0;
		List<Integer> line1 = null, line2 = null;
		int i = 0;
		for (; i < solution.solution.size() - 1; i++) {
			if (!(solution.solution.get(i).isEmpty())) {
				line1 = solution.solution.get(i);
				break;
			}
		}
		i++;
		while (i < solution.solution.size()) {
			if (line2 != null) {
				line1 = line2;
			}
			for (; i < solution.solution.size(); i++) {
				if (!(solution.solution.get(i).isEmpty())) {
					line2 = solution.solution.get(i);
					break;
				}
			}
			if (dl.vehicles[line1.get(0)].series != dl.vehicles[line2.get(0)].series) {
				f++;
			}
			i++;
		}
		return f;
	}

	public int f2(SolutionVector solution) {
		int f = 0;
		for (List<Integer> line : solution.solution) {
			if (!(line.isEmpty())) {
				f++;
			}
		}
		return f;
	}

	public double f3(SolutionVector solution) {
		double f = 0.0;
		List<Integer> line;
		double length;
		for (int i = 0; i < solution.solution.size(); i++) {
			line = solution.solution.get(i);
			if (!(line.isEmpty())) {
				length = dl.vehicles[line.get(0)].length;
				for (int j = 1; j < line.size(); j++) {
					length += dl.vehicles[line.get(j)].length + 0.5;
				}
				f += dl.lines[i].length - length;
			}

		}
		return f;
	}

	private double firstGoal(SolutionVector solution) {
		return p1 * f1(solution) + p2 * f2(solution) + p3 * f3(solution);
	}

	public double g1(SolutionVector solution) {
		double g = 0;
		for (List<Integer> line : solution.solution) {
			for (int i = 0; i < line.size() - 1; i++) {
				if (dl.vehicles[line.get(i)].scheduleType == dl.vehicles[line.get(i + 1)].scheduleType) {
					g++;
				}
			}
		}
		return g;
	}

	public double g2(SolutionVector solution) {
		double g = 0;
		List<Integer> line1 = null, line2 = null;
		int i = 0;
		for (; i < solution.solution.size() - 1; i++) {
			if (!(solution.solution.get(i).isEmpty())) {
				line1 = solution.solution.get(i);
				break;
			}
		}
		i++;
		while (i < solution.solution.size()) {
			if (line2 != null) {
				line1 = line2;
			}
			for (; i < solution.solution.size(); i++) {
				if (!(solution.solution.get(i).isEmpty())) {
					line2 = solution.solution.get(i);
					break;
				}
			}
			if (i < solution.solution.size() && dl.vehicles[line1
					.get(line1.size() - 1)].scheduleType == dl.vehicles[line2.get(0)].scheduleType) {
				g++;
			}
			i++;
		}
		return g;
	}

	public double r3g3(SolutionVector solution) {
		double g = 0;
		int numberOfPairs = 0;
		double vr;
		for (List<Integer> line : solution.solution) {
			if (line.size() >= 2) {
				for (int i = 0; i < line.size() - 1; i++) {
					numberOfPairs++;
					vr = dl.vehicles[line.get(i + 1)].startTime - dl.vehicles[line.get(i)].startTime;
					if (vr >= 10 && vr <= 20) {
						g += 15;
					} else if (vr > 20) {
						g += 10;
					} else {
						g += -4 * (10 - vr);
					}
				}
			}
		}
		double r3 = 1. / (15 * numberOfPairs);

		return r3 * g;
	}

	public double r1(SolutionVector solution) {
		int nrOfVehicles = 0;
		int nrOfUsedLines = 0;
		for (List<Integer> line : solution.solution) {
			if (!(line.isEmpty())) {
				nrOfUsedLines++;
				nrOfVehicles += line.size();
			}
		}
		return 1. / (nrOfVehicles - nrOfUsedLines);
	}

	private double secondGoal(SolutionVector solution) {
		return r1(solution) * g1(solution) + r2 * g2(solution) + r3g3(solution);
	}

	public SolutionVector[] generatePopulation(int popSize) {

		List<Vehicle> vehicles = new ArrayList<>(Arrays.asList(dl.vehicles));
		Random rnd = new Random();
		Vehicle vehicle;
		List<Integer> line = null;
		int unmodified;
		int[] inserted = null;
		Set<Integer> lastLines;

		Collections.sort(vehicles, new Comparator<Vehicle>() {
			@Override
			public int compare(Vehicle v1, Vehicle v2) {
				int value1 = v1.numberOfLines - v2.numberOfLines;
				if (value1 == 0) {
					int value2 = (int) (v2.startTime - v1.startTime);
					if (value2 == 0) {
						return (int) (v2.length - v1.length);
					} else {
						return value2;
					}
				}

				return value1;
			}
		});

		SolutionVector[] population = new SolutionVector[popSize];
		for (int i = 0; i < popSize; i++) {
			List<Vehicle> vehiclesCopy = new ArrayList<>(vehicles);
			SolutionVector solution = new SolutionVector(dl.nrOfLines);
			int lineNumber;
			int shortestVehicle = getShortestVehicleNumber(vehicles);
			List<Integer> emptyLines = new ArrayList<>();
			for (int j = 0; j < dl.nrOfLines; j++) {
				emptyLines.add(j);
			}
			Map<Integer, Set<Integer>> lastSeriesLines = new HashMap<>();
			unmodified = 0;
			while (!(vehiclesCopy.isEmpty())) {

				vehicle = vehiclesCopy.get(0);
				lastLines = lastSeriesLines.get(vehicle.series);
				if (lastLines != null && !(lastLines.isEmpty())) {
					for (int l : lastLines) {
						lineNumber = l;
						inserted = insertVehicle(solution, lineNumber, vehicle.number);
						if (inserted[0] != -1) {
							break;
						}
					}
				}
				if (inserted == null || inserted[0] == -1) {
					if (emptyLines.isEmpty()) {
						i--;
						break;
					}
					do {
						lineNumber = emptyLines.get(rnd.nextInt(emptyLines.size()));
						inserted = insertVehicle(solution, lineNumber, vehicle.number);
						unmodified++;
					} while (inserted[0] == -1 && unmodified < 51);
					if (unmodified > 50) {
						i--;
						break;
					}
					unmodified = 0;
				}

				lineNumber = inserted[0];
				line = solution.solution.get(lineNumber);
				vehiclesCopy.remove(0);
				if (!(vehiclesCopy.isEmpty())) {
					emptyLines.remove(new Integer(lineNumber));
					shortestVehicle = getShortestVehicleNumber(vehiclesCopy);
					if (checkSpace(line, lineNumber, shortestVehicle)) {
						if (lastLines == null) {
							lastLines = new HashSet<>();
							lastSeriesLines.put(vehicle.series, lastLines);
						}
						lastLines.add(lineNumber);
					} else if (lastLines != null) {
						lastLines.remove(lineNumber);
					}
				}
				inserted = null;
			}
			if (vehiclesCopy.isEmpty() && !(Arrays.asList(population).contains(solution))) {
				solution.loss = firstGoal(solution);
				solution.fitness = secondGoal(solution);
				population[i] = solution;
			}
		}

		return population;
	}

	private int[] insertVehicle(SolutionVector solution, int lineNumber, int vehicleNumber) {
		int[] inserted;
		for (int l : dl.lines[lineNumber].blocks) {
			inserted = insertVehicle(solution, l, vehicleNumber);
			if (inserted[0] != -1) {
				return inserted;
			}
		}
		List<Integer> line = solution.solution.get(lineNumber);
		if (checkSpace(line, lineNumber, vehicleNumber) && checkSeries(line, lineNumber, vehicleNumber)
				&& dl.restrictions[vehicleNumber][lineNumber]) {
			for (int i = 0; i < line.size(); i++) {
				if (dl.vehicles[line.get(i)].startTime > dl.vehicles[vehicleNumber].startTime) {
					line.add(i, vehicleNumber);
					if (checkConstraints(solution)) {
						inserted = new int[2];
						inserted[0] = lineNumber;
						inserted[1] = i;
						return inserted;
					} else {
						line.remove(i);
						continue;
					}
				}
			}
			boolean canPutLast = true;
			for (int l : dl.lines[lineNumber].blocks) {
				if (!(solution.solution.get(l).isEmpty())) {
					if (dl.vehicles[solution.solution.get(l).get(0)].startTime < dl.vehicles[vehicleNumber].startTime) {
						canPutLast = false;
						break;
					}
				}
			}
			if (canPutLast) {
				line.add(vehicleNumber);
				if (checkConstraints(solution)) {
					inserted = new int[2];
					inserted[0] = lineNumber;
					inserted[1] = line.size() - 1;
					return inserted;
				} else {
					line.remove(line.size() - 1);
				}
			}
		}

		inserted = new int[2];
		inserted[0] = -1;
		inserted[1] = -1;
		return inserted;
	}

	private boolean checkSpace(List<Integer> line, int lineNumber, int vehicleNumber) {
		double lineCapacity = dl.lines[lineNumber].length;
		double vehiclesLength = 0;
		for (int vehicle : line) {
			vehiclesLength += dl.vehicles[vehicle].length + 0.5;
		}
		vehiclesLength += dl.vehicles[vehicleNumber].length + 0.5;
		vehiclesLength -= 0.5;
		return vehiclesLength <= lineCapacity;
	}

	private boolean checkSeries(List<Integer> line, int lineNumber, int vehicleNumber) {
		if (!(line.isEmpty())) {
			return dl.vehicles[line.get(0)].series == dl.vehicles[vehicleNumber].series;
		}
		return true;
	}

	private SolutionVector lineSwitchMutation(SolutionVector solution) {
		SolutionVector mutated;
		Random rnd = new Random();
		int line1, line2;
		int unmodified = 0;
		do {
			mutated = solution.copyOf();
			line1 = rnd.nextInt(mutated.solution.size());
			line2 = rnd.nextInt(mutated.solution.size());
			Collections.swap(mutated.solution, line1, line2);
			unmodified++;
			System.out.println("Unmodified:" + unmodified);
		} while (!checkConstraints(mutated));
		if (unmodified >= 100) {
			return null;
		}
		return mutated;
	}

	private SolutionVector oneVehicleSwitchMutation(SolutionVector solution) {
		SolutionVector mutated;
		Random rnd = new Random();
		List<Integer> line;
		int pos, linePos, vehicle;
		int[] inserted;
		int unmodified = 0;
		do {
			mutated = solution.copyOf();
			do {
				pos = rnd.nextInt(mutated.solution.size());
				line = mutated.solution.get(pos);
			} while (line.isEmpty());
			linePos = rnd.nextInt(line.size());
			vehicle = line.get(linePos);
			line.remove(linePos);
			inserted = insertVehicle(mutated, pos, vehicle);
			unmodified++;
		} while ((inserted[0] == -1 || !checkConstraints(mutated)) && unmodified < 100);
		if (unmodified >= 100) {
			return null;
		}
		return mutated;
	}

	private SolutionVector vehicleSwitchMutation(SolutionVector solution) {
		SolutionVector mutated;
		Random rnd = new Random();
		List<Integer> line1, line2;
		int pos1, pos2, linePos1, linePos2, vehicle1, vehicle2;
		int[] inserted1, inserted2;
		int unmodified = 0;
		do {
			mutated = solution.copyOf();
			do {
				pos1 = rnd.nextInt(mutated.solution.size());
				line1 = mutated.solution.get(pos1);
			} while (line1.isEmpty());
			do {
				pos2 = rnd.nextInt(mutated.solution.size());
				line2 = mutated.solution.get(pos2);
			} while (line2.isEmpty() || line1.equals(line2));
			linePos1 = rnd.nextInt(line1.size());
			linePos2 = rnd.nextInt(line2.size());
			vehicle1 = line1.get(linePos1);
			line1.remove(linePos1);
			vehicle2 = line2.get(linePos2);
			line2.remove(linePos2);
			inserted1 = insertVehicle(solution, pos1, vehicle2);
			inserted2 = insertVehicle(solution, pos2, vehicle1);
			unmodified++;
		} while ((inserted1[0] == -1 || inserted2[0] == -1 || !checkConstraints(mutated)) && unmodified < 100);
		if (unmodified >= 100) {
			return null;
		}
		return mutated;
	}

	private SolutionVector crossover(SolutionVector s1, SolutionVector s2) {
		SolutionVector child;
		int line, position, vehicle;
		Random rnd = new Random();
		int[] inserted;
		int unmodified = 0;
		do {
			child = s1.copyOf();
			do {
				line = rnd.nextInt(s2.solution.size());
			} while (s2.solution.get(line).isEmpty());
			position = rnd.nextInt(s2.solution.get(line).size());
			vehicle = s2.solution.get(line).get(position);
			for (int i = 0; i < child.solution.size(); i++) {
				List<Integer> l = child.solution.get(i);
				for (int j = 0; j < l.size(); j++) {
					if (l.get(j) == vehicle) {
						l.remove(j);
					}
				}
			}
			inserted = insertVehicle(child, line, vehicle);
			unmodified++;
		} while ((inserted[0] == -1 || !checkConstraints(child)) && unmodified < 100);
		if (unmodified >= 100) {
			return null;
		}
		return child;
	}

}
