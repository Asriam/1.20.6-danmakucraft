import com.adrian.thDanmakuCraft.lua.LuaCore;
import org.luaj.vm2.Globals;

public class jsTest {


    public static void main(String[] args){
        Globals globals = LuaCore.getGlobals();
        String script = "bullet_styles = {\n" +
                "    arrow_big = \"arrow_big\",\n" +
                "    arrow_mid = \"arrow_mid\",\n" +
                "    arrow_small = \"arrow_small\",\n" +
                "    gun_bullet = \"gun_bullet\",\n" +
                "    butterfly = \"butterfly\",\n" +
                "    square = \"square\",\n" +
                "    ball_small = \"ball_small\",\n" +
                "    ball_mid = \"ball_mid\",\n" +
                "    ball_mid_c = \"ball_mid_c\",\n" +
                "    ball_big = \"ball_big\",\n" +
                "    ball_huge = \"ball_huge\",\n" +
                "    ball_light = \"ball_light\",\n" +
                "    star_small = \"star_small\",\n" +
                "    star_big = \"star_big\",\n" +
                "    grain_a = \"grain_a\",\n" +
                "    grain_b = \"grain_b\",\n" +
                "    grain_c = \"grain_c\",\n" +
                "    kite = \"kite\",\n" +
                "    knife = \"knife\",\n" +
                "    knife_b = \"knife_b\",\n" +
                "    water_drop = \"water_drop\",\n" +
                "    mildew = \"mildew\",\n" +
                "    ellipse = \"ellipse\",\n" +
                "    heart = \"heart\",\n" +
                "    money = \"money\",\n" +
                "    music = \"music\",\n" +
                "    silence = \"silence\",\n" +
                "    water_drop_dark = \"water_drop_dark\",\n" +
                "    ball_huge_dark = \"ball_huge_dark\",\n" +
                "    ball_light_dark = \"ball_light_dark\",\n" +
                "}\n" +
                "\n" +
                "print(\"aaa\")";

        globals.load(script).call();
    }
}
