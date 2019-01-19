
public class Vehicle {

	public int number;
	public double length;
	public int series;
	public double startTime;
	public int scheduleType;
	public int numberOfLines;

	public Vehicle(int number) {
		this.number = number;
		numberOfLines = 0;
	}

	public Vehicle(double length) {
		this.length = length;
		numberOfLines = 0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Number: " + number + "\n");
		sb.append("Length: " + length + "\n");
		sb.append("Vehicle series: " + series + "\n");
		sb.append("Start time: " + startTime + "\n");
		sb.append("Schedule type: " + scheduleType + "\n");
		sb.append("Number of lines: " + numberOfLines + "\n");
		return sb.toString();
	}
}
