package hhx.group.foodhealth;

/**
 * Created by Xiaoting Huang on 2017-10-4.
 * Store Category data
 */

public class Category {

    private String id;
    private String name;
    private String image;


    public Category(String id, String name, String image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }
}
