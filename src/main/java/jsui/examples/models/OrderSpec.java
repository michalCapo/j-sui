package jsui.examples.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderSpec {
    public String Field;
    public String Direction;

    public OrderSpec(String field, String direction) {
        this.Field = field;
        this.Direction = direction;
    }
}
