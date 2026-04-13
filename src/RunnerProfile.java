public class RunnerProfile {
    private int height;
    private int weight;
    private double legLength;

    public RunnerProfile(int height, int weight) {
        this.height = height;
        this.weight = weight;
    }

    public RunnerProfile(int height, int weight, int legLength) {
        this.legLength = legLength;
        this.height = height;
        this.weight = weight;
    }

    public double[] getCadences() {
        double[] cadences = new double[3];
        cadences[0] = calcCadence(10.67);
        cadences[1] = calcCadence(7.63);
        cadences[2] = calcCadence(5.37);
        return cadences;
    }

    public double calcCadence(double minsPerMile) {
        // Convert pace to meters per second because that's what formula uses
        double speedMps = 1609.34 / (minsPerMile * 60);

        // Convert height (inches) to leg length (cm)
        if (legLength == 0) {
            legLength = height * .45;
        }

        // Convert legLength to cm for formula
        double legLengthCm = legLength * 2.54;

        // Taylor-Haas regression formula
        // "start at 254.858 steps per minute, subtract about 1.25 for every cm of leg length (taller = fewer steps),
        // add about 3.67 for every m/s of speed (faster = more steps)"
        double baseCadence = (-1.251 * legLengthCm) + (3.665 * speedMps) + 254.858;

        // Slight adjustment based on your weight
        double weightAdj = 1.0 + 0.04 * ((154.0 - weight) / 154.0);

        return baseCadence * weightAdj;
    }


    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
