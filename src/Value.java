import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;

public class Value implements Serializable {


    private String trackName;
    private String artistName;
    private String albumInfo;
    private String genre;
    private int totalBytes;
    private byte[] musicFileExtract;

    public Value(MusicFile musicFile,int i){
        this.trackName = musicFile.getTrackName();
        this.artistName = musicFile.getArtistName();
        this.albumInfo = musicFile.getAlbumInfo();
        musicFileExtract = Arrays.copyOfRange(musicFile.getMusicFileExtract(),i,i+512);
    }

    public int getTotalBytes(){
        return totalBytes;
    }

    public void setTotalBytes(int totalBytes){
        this.totalBytes = totalBytes;
    }

    public void setTrackName(String trackName){
        this.trackName=trackName;
    }

    public void setArtistName(String artistName){
        this.artistName=artistName;
    }

    public void setAlbumInfo(String albumInfo){
        this.albumInfo=albumInfo;
    }
    public void setGenre(String genre){
        this.genre=genre;
    }
    public void setMusicFileExtract(byte [] musicFileExtract){
        this.musicFileExtract=musicFileExtract;
    }
    public String  getTrackName(){
        return trackName;
    }

    public String getArtistName(){
        return artistName;
    }

    public String getAlbumInfo(){
        return albumInfo;
    }

    public String getGenre(){
        return genre;
    }

    public byte[] getMusicFileExtract(){
        return musicFileExtract;
    }

}