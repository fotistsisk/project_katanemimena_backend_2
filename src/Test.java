/*




import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import org.apache.commons.codec.digest.DigestUtils;
import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.ID3v1;


import javax.swing.*;
import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class Test {
    private static MediaPlayer mediaPlayer;
    private static String mp3_location = "E:\\Billie Eilish - WHEN WE ALL FALL ASLEEP, WHERE DO WE GO (2019)\\06. wish you were gay.mp3";

    public static void main(String[] args) throws InterruptedException {

        //wait to initialize toolkit
        final CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new JFXPanel(); // initializes JavaFX environment
                latch.countDown();
            }
        });
        latch.await();

        //Publisher p = new Publisher();
        //p.init(1);
        //Value v = p.songs.get(9);
        MusicFile m = v.getmusicFile();
        //playMp3(m.getMusicFileExtract());

        BigInteger hash = new BigInteger(DigestUtils.sha1Hex("Ariana Grande"),16);
        BigInteger HUNDREAD = new BigInteger("1000");
        hash = hash.mod(HUNDREAD);
        System.out.println(hash);




        String passwordToHash = "100.011.031:4050";
        String generatedPassword = null;
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            //Add password bytes to digest
            md.update(passwordToHash.getBytes());
            //Get the hash's bytes
            byte[] bytes = md.digest();
            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            //Get complete hashed password in hex format
            generatedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        BigInteger hash2 = new BigInteger(generatedPassword,16);
        //System.out.println(hash2);
        //System.out.println(generatedPassword);





        /*

        try{
            File f = new File(mp3_location);
            FileInputStream is = new FileInputStream(f);
            byte[] byte_array = new byte[(int) f.length()];
            is.read(byte_array);

            //metadata
            MP3File mp3file = new MP3File(f);
            ID3v1 id3v2 = mp3file.getID3v1Tag();
            System.out.println(id3v2.getArtist());
            System.out.println("----------------------------------------------");
            System.out.println("Title: " + id3v2.getSongTitle());
            System.out.println("Artists: " + id3v2.getArtist());
            System.out.println("Album : "+id3v2.getAlbumTitle());





            //byte_array = inputStreamToByteArray(is);
            is.close();
            //System.out.println(Arrays.toString(byte_array));



            playMp3(byte_array);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TagException e) {
            e.printStackTrace();
        }



    }





    public static void playMp3(byte[] mp3SoundByteArray) {
        try {

            // create temp file that will hold byte array
            File tempMp3 = File.createTempFile("kurchina", "mp3");
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.close();

            Media hit = new Media(tempMp3.toURI().toString());
            mediaPlayer = new MediaPlayer(hit);
            mediaPlayer.play();

        } catch (IOException ex) {
            String s = ex.toString();
            ex.printStackTrace();
        }
    }
}

*/
