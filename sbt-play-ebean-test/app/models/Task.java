package models;

import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.WhoCreated;
import io.ebean.annotation.WhoModified;
import play.data.format.Formats;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class Task extends Model {

    public static Finder<Long, Task> find = new Finder<>(Task.class);

    @Id
    public Long id;

    @Constraints.Required
    public String name;

    @WhoCreated
    public String whoCreated;

    @WhoModified
    public String whoModified;

    public boolean done;

    @Formats.DateTime(pattern = "dd/MM/yyyy")
    public Date dueDate = new Date();
}
