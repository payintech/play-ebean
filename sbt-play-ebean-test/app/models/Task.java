package models;

import com.avaje.ebean.Model;
import play.data.format.Formats;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class Task extends Model {

    public static Model.Finder<Long, Task> find = new Model.Finder<>(Task.class);

    @Id
    public Long id;

    @Constraints.Required
    public String name;

    public boolean done;

    @Formats.DateTime(pattern = "dd/MM/yyyy")
    public Date dueDate = new Date();
}
