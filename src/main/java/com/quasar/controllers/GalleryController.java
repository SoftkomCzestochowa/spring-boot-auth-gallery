package com.quasar.controllers;

import com.quasar.GalleryApplication;
import com.quasar.files.FileHandler;
import com.quasar.model.Album;
import com.quasar.model.Image;
import com.quasar.repository.Repository;
import com.quasar.service.AlbumService;
import com.quasar.service.ImageService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class GalleryController {
	
    private static SortedSet<Album> albums = new TreeSet<>();
    
    @Autowired
    private AlbumService albumService;
    
    @Autowired
    private FileHandler fileHandler;
    
    @Value("${gallery.directory}")
    String homeDirectory;

    @Autowired
	private ImageService imageService;

    public GalleryController() {
    }

    @GetMapping("/album/refresh")
    public ModelAndView getRefreshAlbums(@RequestParam Optional<String> error) {
        albums.clear();
        File galleryHomeDirectory = new File(GalleryApplication.getGalleryHomeDirectory() == null ? homeDirectory : GalleryApplication.getGalleryHomeDirectory());
        File[] albumFileList = galleryHomeDirectory.listFiles((filex) -> {
            return filex.isDirectory();
        });
        if (albumFileList != null) {
            File[] var4 = albumFileList;
            int var5 = albumFileList.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                File directory = var4[var6];
                List<Image> images = new ArrayList<>();
                File[] imageFileList = directory.listFiles((filex) -> {
                    return !filex.isDirectory() && filex.toString().toLowerCase().endsWith("jpg");
                });
                Album album = this.checkIfUUIDAlreadyCreated(directory, images);
                this.fileHandler.createThumbnailDirectory(directory);
                if (imageFileList != null) {
                    System.out.printf("Processing directory %s, image qty: %d%n", directory.getName(), imageFileList.length);

                    for(int i = 0; i < imageFileList.length; ++i) {
                        File file = imageFileList[i];
                        Image image = new Image(file, album.getAlbumid().toString(), this.fileHandler);
                        images.add(image);
                        this.fileHandler.createThumbnail(file);
                        if (images.size() > 1) {
                            Image previousImage = (Image)images.get(images.size() - 2);
                            previousImage.setNextId(image.getId());
                            Image prevImageFromDb = imageService.getImageById(previousImage.getId());
                            prevImageFromDb.setNextId(image.getId());
                            imageService.save(prevImageFromDb);
                            image.setPreviousId(previousImage.getId());
                        }
                        imageService.save(image);
                    }
                }

                if (images.size() > 1) {
                    ((Image)images.get(0)).setPreviousId(((Image)images.get(images.size() - 1)).getId());
                    ((Image)images.get(images.size() - 1)).setNextId(((Image)images.get(0)).getId());
                }

                album.setImages(images);
                System.out.printf("Album %s [ID: %s] created with %d images%n", album.getName(), album.getAlbumid(), images.size());
                albums.add(album);
            }
        }

        Repository.setAlbums(albums);
        return new ModelAndView("redirect:/gallery");
    }

    private Album checkIfUUIDAlreadyCreated(File directory, List<Image> images) {
        File uuidFile = new File(directory + File.separator + "uuid");
        Album album = new Album(directory, images);
        if (uuidFile.exists()) {
            try {
                album.setId((String)Files.readAllLines(uuidFile.toPath()).get(0));
            } catch (IOException var6) {
                var6.printStackTrace();
            }

            return album;
        } else {
            this.fileHandler.createUUIDFile(album);
            return this.albumService.save(album);
        }
    }

    @GetMapping("/save")
    public ModelAndView saveAlbum() {
        this.albumService.save(new Album(new File("C:/temp/1"), new ArrayList<>()));
        Map<String, Object> map = new HashMap<>();
        return new ModelAndView("gallery", map);
    }

    @GetMapping("/gallery")
    public ModelAndView getAllAlbums(@RequestParam Optional<String> error) {
        if (albums.isEmpty()) {
            albums = this.albumService.getAlbums();
        }

        System.out.printf("Loaded %d albums from database", albums.size());
        Map<String, Object> map = new HashMap<>();
        map.put("albums", albums);
        return new ModelAndView("gallery", map);
    }

    @GetMapping("/gallery2")
    public ModelAndView getGallery2(@RequestParam Optional<String> error) {
        if (albums.isEmpty()) {
            albums = this.albumService.getAlbums();
        }

        Map<String, Object> map = new HashMap<>();
        map.put("albums", albums);
        return new ModelAndView("gallery2", map);
    }

    @GetMapping("/album2")
    public ModelAndView getAllAlbums2(@RequestParam Optional<String> error) {
        if (albums.isEmpty()) {
            albums = this.albumService.getAlbums();
        }

        Map<String, Object> map = new HashMap<>();
        map.put("albums", albums);
        return new ModelAndView("gallery2", map);
    }

    @RequestMapping(
        path = {"/album/{albumName}/{albumId}"},
        method = {RequestMethod.GET}
    )
    public ModelAndView getImagesForAlbum(@PathVariable String albumName, @PathVariable String albumId, @RequestParam Optional<String> error) {
        Map<String, Object> map = new HashMap<>();
        Set<Image> imagesForAlbum = Repository.getImagesForAlbum(albumId);
        System.out.printf("get images for album: [%s] %s, images %d%n", albumId, albumName, imagesForAlbum.size());
        map.put("images", imagesForAlbum);
        return new ModelAndView("album", map);
    }

    @GetMapping("/picture/{albumId}/{imageId}")
    public ModelAndView showImage(@PathVariable String albumId, @PathVariable String imageId, @RequestParam Optional<String> error) {
        Map<String, Object> map = new HashMap<>();
        System.out.println("get images for albumId: " + albumId + ", image: " + imageId);
        map.put("albumName", Repository.getAlbum(albumId).getName());
        map.put("albumId", albumId);
        map.put("imageId", imageId);
        return new ModelAndView("picture", map);
    }
}
