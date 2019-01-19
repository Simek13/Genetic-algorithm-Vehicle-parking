import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class DataLoader {

	public String file;
	public int nrOfVehicles;
	public int nrOfLines;
	public Vehicle[] vehicles;
	public Line[] lines;
	public boolean[][] restrictions;

	public DataLoader(String file) {
		this.file = file;
	}

	public void load() throws IOException {

		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		} catch (FileNotFoundException e) {
			System.err.println("Datoteka ne postoji.");
			System.exit(-1);
		}
		String line;
		Scanner sc;

		line = br.readLine().trim();
		this.nrOfVehicles = Integer.parseInt(line);

		line = br.readLine().trim();
		this.nrOfLines = Integer.parseInt(line);

		vehicles = new Vehicle[nrOfVehicles];
		lines = new Line[nrOfLines];
		restrictions = new boolean[nrOfVehicles][nrOfLines];

		br.readLine();

		line = br.readLine();
		sc = new Scanner(line);
		int i = 0;
		while (sc.hasNextInt()) {
			vehicles[i] = new Vehicle(i);
			vehicles[i++].length = sc.nextInt();
		}
		sc.close();

		br.readLine();

		line = br.readLine();
		sc = new Scanner(line);
		i = 0;
		while (sc.hasNextInt()) {
			vehicles[i++].series = sc.nextInt();
		}
		sc.close();

		br.readLine();

		int j = 0;
		while (!(line = br.readLine().trim()).equals("")) {
			sc = new Scanner(line);
			i = 0;
			while (sc.hasNextInt()) {
				restrictions[j][i++] = sc.nextInt() == 1;
			}
			sc.close();
			j++;
		}

		line = br.readLine();
		sc = new Scanner(line);
		i = 0;
		while (sc.hasNextInt()) {
			lines[i] = new Line();
			lines[i++].length = sc.nextInt();
		}
		sc.close();

		br.readLine();

		line = br.readLine();
		sc = new Scanner(line);
		i = 0;
		while (sc.hasNextInt()) {
			vehicles[i++].startTime = sc.nextInt();
		}
		sc.close();

		br.readLine();

		line = br.readLine();
		sc = new Scanner(line);
		i = 0;
		while (sc.hasNextInt()) {
			vehicles[i++].scheduleType = sc.nextInt();
		}
		sc.close();

		br.readLine();

		int blockingLine;
		while ((line = br.readLine()) != null) {
			sc = new Scanner(line);
			blockingLine = sc.nextInt();
			i = 0;
			while (sc.hasNextInt()) {
				lines[blockingLine - 1].blocks.add(sc.nextInt() - 1);
			}
			sc.close();
		}
		for (i = 0; i < nrOfVehicles; i++) {
			for (j = 0; j < nrOfLines; j++) {
				if (restrictions[i][j]) {
					vehicles[i].numberOfLines++;
				}
			}
		}

		br.close();
	}

}
