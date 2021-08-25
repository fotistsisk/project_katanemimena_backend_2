import java.io.Serializable;

public class SongRequest implements Serializable {
    private String song,artist;

    public SongRequest() {
        song = null;
        artist = null;
    }

    public SongRequest(String artist) {
        song = null;
        this.artist = artist;
    }

    public SongRequest(String artist,String song) {
        this.song = song;
        this.artist = artist;
    }

    public String getSong(){
        return song;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setSong(String song) {
        this.song = song;
    }

}
