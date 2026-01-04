package jsui.examples.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DemoForm {
    @NotBlank(message = "Name is required")
    public String Name = "";
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    public String Email = "";
    public String Phone = "";
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    public String Password = "";
    public double Age = 0;
    public double Price = 0;
    public String Bio = "";
    public String Gender = "";
    public String Country = "";
    public boolean Agree = false;
    public java.util.Date BirthDate = new java.util.Date();
    public java.util.Date AlarmTime = new java.util.Date();
    public java.util.Date Meeting = new java.util.Date();
}
