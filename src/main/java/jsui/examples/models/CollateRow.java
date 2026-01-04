package jsui.examples.models;

import lombok.Data;

@Data
public class CollateRow {
    public int ID;
    public String Name;
    public String Email;
    public String Avatar;
    public String Country;
    public String City;
    public String Job;
    public String Bio;
    public String Role;
    public java.util.Date CreatedAt;
    public boolean Active;
    public String Status;

    public CollateRow copy() {
        CollateRow r = new CollateRow();
        r.ID = ID;
        r.Name = Name;
        r.Email = Email;
        r.Avatar = Avatar;
        r.Country = Country;
        r.City = City;
        r.Job = Job;
        r.Bio = Bio;
        r.Role = Role;
        r.CreatedAt = CreatedAt != null ? new java.util.Date(CreatedAt.getTime()) : null;
        r.Active = Active;
        r.Status = Status;
        return r;
    }
}
