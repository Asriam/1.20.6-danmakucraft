import org.luaj.vm2.script.LuaScriptEngineFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import static com.adrian.thDanmakuCraft.JSCore.ENGINE_NAME;

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
        System.out.print("aaa");
    }
}
