import com.adrian.thDanmakuCraft.script.lua.LuaCore;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.PackType;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;

import java.util.ArrayList;
import java.util.List;

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

        /*
        Globals globals = LuaCore.getGlobals();

        String script =
                        "print('s')" +
                        "aaa = function()" +
                        "   print('11111sad')" +
                        "end";
        LuaValue chunk = globals.load(script).call();
        globals.get("aaa").checkfunction().invoke();

         */
        List<Integer> list = tsasda.list;

        Thread thread = new Thread(()->{
            for(int i=0; i<150; i++){
                System.out.println(i);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        thread.start();
        System.out.println(list.stream().filter(integer -> integer == 1).toList().getFirst() + "aaaaaaaaaaaa");


        /*
        for(int i: list){
            if(i == 1){
                System.out.println(i + "aaaaaaaaaaaa");
                break;
            }
        }*/

        /*
        for(int i=0;i<list.size();i++){
            if(list.get(i) == 1){
                System.out.println(list.get(i) + "aaaaaaaaaaaa");
                break;
            }
        }*/
        //System.out.println(list);
    }
}
