import java.util.ArrayList;

public class Song {
    private String title;
    private String artist;
    private int bpm;

    // Song duration in seconds
    private int duration;
    private String genre;
    private int energyLevel;
    private int bpmMatchRatio;


    public Song(String title, String artist, int bpm, int duration, String genre, int energyLevel) {
        this.title = title;
        this.artist = artist;
        if (bpm > 199) {
            while (bpm > 199) {
                bpm /= 2;
            }
        }
        else if (bpm < 100) {
            while (bpm < 100) {
                bpm *= 2;
            }
        }
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

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public int getEnergyLevel() { return energyLevel; }
    public void setEnergyLevel(int energyLevel) { this.energyLevel = energyLevel; }

    public int getBpmMatchRatio() {
        return bpmMatchRatio;
    }

    public void setBpmMatchRatio(int newBpm) {
        this.bpmMatchRatio = newBpm;
    }
}
