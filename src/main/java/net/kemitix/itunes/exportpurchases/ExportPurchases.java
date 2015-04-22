package net.kemitix.itunes.exportpurchases;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.kemitix.itunes.medialibrary.AlbumTrack;
import net.kemitix.itunes.medialibrary.ITunesMediaLibrary;
import net.kemitix.itunes.medialibrary.MediaLibrary;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

public class ExportPurchases {

    private MediaLibrary library;

    private String mediaLibraryFile;
    private String purchasesDirectory;
    private String exportedDirectory;

    public static void main(String[] args) {
        ExportPurchases app = new ExportPurchases();
        app.run(args);
    }

    public void run(String[] args) {
        mediaLibraryFile = args[0];
        purchasesDirectory = args[1];
        exportedDirectory = args[2];

        library = ITunesMediaLibrary.createLibrary(mediaLibraryFile);

        List<File> purchasedFiles = getPurchasedFiles();
        purchasedFiles.stream().forEach((File file) -> exportAlbumTrack(file));
    }

    private List<File> getPurchasedFiles() {
        return Arrays.asList(new File(purchasesDirectory).listFiles());
    }

    private void exportAlbumTrack(File file) {
        AlbumTrack albumTrack = library.findAlbumTrack(file);
        if (albumTrack == null) {
            System.out.println("Skipped: " + file.getName());
            return;
        }
        System.out.println("Found: " + albumTrack.getTrackTitle());
        new File(exportedDirectory).mkdirs();
        File newFile = new File(exportedDirectory + "/" + albumTrack.getTrackTitle() + ".m4a");

        try {

            Files.copy(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            AudioFile audioFile = AudioFileIO.read(newFile);
            Tag tags = audioFile.getTagOrCreateAndSetDefault();

            tags.setField(FieldKey.ALBUM_ARTIST, albumTrack.getAlbumArtist());
            tags.setField(FieldKey.ALBUM, albumTrack.getAlbumTitle());
            tags.setField(FieldKey.ARTIST, albumTrack.getTrackArtist());
            tags.setField(FieldKey.TITLE, albumTrack.getTrackTitle());

            audioFile.setTag(tags);
            audioFile.commit();

        } catch (IOException | CannotReadException | TagException | ReadOnlyFileException | InvalidAudioFrameException | CannotWriteException ex) {
            Logger.getLogger(ExportPurchases.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
