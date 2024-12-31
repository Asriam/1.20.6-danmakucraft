import com.adrian.thDanmakuCraft.script.lua.LuaCore;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.PackType;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;

public class jsTest {

    public static void main(String[] args){
        /*
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        scriptEngineManager.registerEngineName(ENGINE_NAME,new LuaScriptEngineFactory());
        ScriptEngine engine = scriptEngineManager.getEngineByName(ENGINE_NAME);
        try {
            engine.eval("print(\"hello world\")");
        } catch (ScriptException e) {
            e.printStackTrace();
        }*/
        //ResourceManager resourceManager = new ReloadableResourceManager(PackType.CLIENT_RESOURCES);

        Globals globals = LuaCore.getGlobals();

        String script =
                        "print('s')" +
                        "aaa = function()" +
                        "   print('11111sad')" +
                        "end";
        LuaValue chunk = globals.load(script).call();
        globals.get("aaa").checkfunction().invoke();
    }
}
