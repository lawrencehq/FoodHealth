package hhx.group.foodhealth;

/**
 * Created by Xiaoting Huang on 2017-10-3.
 */

public class Food {

    private String id;
    private String cid;
    private String name;
    private String image;
    private double energy;
    private double protein;
    private double fat;
    private double carbohydrates;
    private double dietary_fiber;
    private String description;


    public Food(String id, String cid, String name, String image, double energy, double protein, double fat, double carbohydrates, double dietary_fiber, String description) {
        this.id = id;
        this.cid = cid;
        this.name = name;
        this.image = image;
        this.energy = energy;
        this.protein = protein;
        this.fat = fat;
        this.carbohydrates = carbohydrates;
        this.dietary_fiber = dietary_fiber;
        this.description = description;
    }


    public String getId() {
        return id;
    }

    public String getCid() {
        return cid;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public double getEnergy() {
        return energy;
    }

    public double getProtein() {
        return protein;
    }

    public double getFat() {
        return fat;
    }

    public double getCarbohydrates() {
        return carbohydrates;
    }

    public double getDietary_fiber() {
        return dietary_fiber;
    }

    public String getDescription() {
        return description;
    }
}
