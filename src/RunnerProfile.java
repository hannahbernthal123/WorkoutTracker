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


    public double calcCadenceFromPace(int minutes, int seconds) {
        double minsPerMile = minutes + (seconds / 60.0);
        return calcCadence(minsPerMile);
    }

    public double calcCadence(double minsPerMile) {
        // Convert pace to meters per second
        double speedMps = 1609.34 / (minsPerMile * 60);

        // height is in inches, convert to cm first, THEN take 45%
        double heightCm = height * 2.54;
        if (legLength == 0) {
            legLength = heightCm * 0.45; // leg length in cm
        }

        // legLength is now already in cm, no need to multiply by 2.54 again
        double legLengthCm = legLength;

        // Taylor-Haas formula
        double baseCadence = (-1.251 * legLengthCm) + (3.665 * speedMps) + 254.858;

        // Weight adjustment
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
