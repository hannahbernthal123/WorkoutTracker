public class Song {
    private String title;
    private String artist;
    private int bpm;

    // Song duration in minutes
    private double duration;
    private String genre;
    private int energyLevel;

    public Song(String title, String artist, int bpm, double duration, String genre, int energyLevel) {
        this.title = title;
        this.artist = artist;
        this.bpm = bpm;
        this.duration = duration;
        this.genre = genre;
        this.energyLevel = energyLevel;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public int getBpm() { return bpm; }
    public void setBpm(int bpm) { this.bpm = bpm; }

    public double getDuration() { return duration; }
    public void setDuration(double duration) { this.duration = duration; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public int getEnergyLevel() { return energyLevel; }
    public void setEnergyLevel(int energyLevel) { this.energyLevel = energyLevel; }
}
