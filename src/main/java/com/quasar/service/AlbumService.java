package com.quasar.service;

import com.quasar.model.Album;
import java.util.SortedSet;
import org.springframework.cache.annotation.Cacheable;

public interface AlbumService {
    Album save(Album var1);

    @Cacheable({"albums"})
    Album getAlbumById(String var1);

    SortedSet<Album> getAlbums();

    void renameAlbum(String var1, String var2);
    
    SortedSet<Album> getAlbumsForUser(String userId);
}
