package chiloven.xamlsorter.entities;

import java.io.Serializable;

public class ProjectMeta implements Serializable {
    private String name;
    private String description;
    private String author;

    public ProjectMeta() {
    }

    /**
     * Create a new ProjectMeta instance with the specified name, description, and author.
     *
     * @param name        the name of the project
     * @param description the description of the project
     * @param author      the author of the project
     */
    public ProjectMeta(String name, String description, String author) {
        this.name = name;
        this.description = description;
        this.author = author;
    }


    /**
     * Get the name of the project.
     *
     * @return the name of the project
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the project.
     *
     * @param name the name to set for the project
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the description of the project.
     *
     * @return the description of the project
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description of the project.
     *
     * @param description the description to set for the project
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the author of the project.
     *
     * @return the author of the project
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Set the author of the project.
     *
     * @param author the author to set for the project
     */
    public void setAuthor(String author) {
        this.author = author;
    }

}
