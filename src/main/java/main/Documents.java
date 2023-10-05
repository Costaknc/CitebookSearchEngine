package main;

import java.io.File;

/**
 *
 * @author csd3829
 */
public class Documents {

    private String Title;
    private String Authors;
    private String Description;
    private String Keywords;

    File file;

    Documents(String new_title, String author, String abstr, String key, File new_file) {
        this.Title = new_title;
        this.Authors = author;
        this.Description = abstr;
        this.Keywords = key;

        this.file = new_file;
    }

    public String getTitle() {
        return this.Title;
    }

    public String getAuth() {
        return this.Authors;
    }

    public String getDes() {
        return this.Description;
    }

    public String getKey() {
        return this.Keywords;
    }

    public File getFile() {
        return this.file;
    }

    public String getPath() {
        return this.file.getAbsolutePath();
    }

    public void setTitle(String title) {
        this.Title = title;
    }

    public void setAuthors(String auth) {
        this.Authors = auth;
    }

    public void setDes(String des) {
        this.Description = des;
    }

    public void setKey(String key) {
        this.Keywords = key;
    }
}
