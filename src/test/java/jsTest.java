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
        Globals globals = LuaCore.getGlobals();

        String script = "print('fffffffffffffffffffffffff')"+
                        "local co = coroutine.create(function()\n" +
                                "    for i=0,2 do\n" +
                                "        print(''..i..'aaaaaaaaaaaaaaaaaaaaa')\n" +
                                "coroutine.yield()" +
                                "    end\n" +
                                "end)\n" +
                                "\n" +
                "coroutine.resume(co)"+
                "coroutine.resume(co)"+
                "coroutine.resume(co)"+
                "coroutine.resume(co)"+
                "coroutine.resume(co)";
        globals.load(script).call();
    }
}
