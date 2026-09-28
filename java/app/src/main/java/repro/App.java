package repro;

import com.pulumi.Context;
import com.pulumi.Pulumi;
import com.pulumi.random.RandomPet;
import com.pulumi.random.RandomPetArgs;
import com.pulumi.random.RandomString;
import com.pulumi.random.RandomStringArgs;

public class App {
    public static void main(String[] args) {
        Pulumi.run(App::stack);
    }

    private static void stack(Context ctx) {
        var a = new RandomString("a", RandomStringArgs.builder()
                .length(8)
                .special(false)
                .build());

        var b = new RandomPet("b", RandomPetArgs.builder()
                .prefix(a.result())
                .build());

        ctx.export("aResult", a.result());
        ctx.export("bId", b.id());
    }
}
