package com.quasar.model;

import java.io.File;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(
    name = "album"
)
public class Album implements Comparable<Album> {
    @Id
    @Column(
        length = 36,
        unique = true,
        nullable = false
    )
    private String albumid;
    private String name;
    private String path;
    private Date created_date;
    @Transient
    private Map<String, Image> images = new HashMap<>();

    public Album() {
    }

    public Album(File directory, List<Image> images) {
        this.albumid = UUID.randomUUID().toString();
        this.name = directory.getName().replace("_", "-");
        this.path = directory.getPath();
        Iterator<Image> var3 = images.iterator();

        while(var3.hasNext()) {
            Image i = (Image)var3.next();
            this.images.put(i.getId(), i);
        }

        this.updateCreatedDate();
    }

    private boolean isWhitespaceOrSimilar(char charAt) {
        switch(charAt) {
        case '\t':
        case ' ':
        case '-':
        case '_':
            return true;
        default:
            return false;
        }
    }

    public void setImages(List<Image> images) {
        System.out.printf("Setting images for Album [name: %s], [id: %s], [image qty: %d]%n", this.getName(), this.getAlbumid(), images.size());
        Iterator<Image> var2 = images.iterator();

        while(var2.hasNext()) {
            Image i = (Image)var2.next();
            this.images.put(i.getId(), i);
        }

        this.updateCreatedDate();
    }

    private void updateCreatedDate() {
        if (this.created_date == null) {
            try {
                this.created_date = Date.valueOf(this.name.substring(0, 10));
            } catch (StringIndexOutOfBoundsException | IllegalArgumentException var3) {
                Optional<String> firstKey = this.images.keySet().stream().findFirst();
                if (firstKey.isPresent()) {
                    this.created_date = ((Image)this.images.get(firstKey.get())).getDateTaken();
                    this.name = this.created_date.toString() + " " + this.name;
                }
            }
        }

    }

    public String getAlbumid() {
        return this.albumid;
    }

    public void setId(String id) {
        this.albumid = id;
        System.out.printf("Setting id %s for album %s%n", id, this.name);
    }

    public String getName() {
        return this.name;
    }

    public String getPath() {
        return this.path;
    }

    public synchronized LocalDate getCreated_date() {
        return this.created_date.toLocalDate();
    }

    public int compareTo(Album o) {
        return this.name.compareTo(o.getName());
    }

    public Map<String, Image> getImages() {
        return this.images;
    }

    public void rename(File newNameAlbum) {
        this.path = newNameAlbum.getPath();
    }
}