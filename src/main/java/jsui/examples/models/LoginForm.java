package jsui.examples.models;

import lombok.Data;

@Data
public class LoginForm {
    public String Name = "";
    public String Email = "";
    public String Password = "";
    public boolean RememberMe = false;
}
