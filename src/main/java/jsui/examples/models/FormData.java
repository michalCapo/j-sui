package jsui.examples.models;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FormData {
    public String Title = "";
    public String Some = "";
    public String Gender = "";
    public String GenderNext = "";
    public double Number = 0;
    public String Country = "";
    public boolean Agree = false;
    @NotBlank(message = "Field is required")
    public String value = "";
}
