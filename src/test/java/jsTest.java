import com.adrian.thDanmakuCraft.lua.LuaCore;
import org.luaj.vm2.Globals;

public class jsTest {


    public static void main(String[] args){
        Globals globals = LuaCore.getGlobals();
        String script =
                "print('fffffffffffffffffffffffff')"+
                "local co = coroutine.create(function()\n" +
                "    for i=0,2 do\n" +
                "       print(''..i..'aaaaaaaaaaaaaaaaaaaaa')\n" +
                //        "print(coroutine.status(co))" +
                "       coroutine.yield()\n" +
                "    end\n" +
                "end)\n" +
                "\n" +
                "coroutine.resume(co)" +
                        "print(coroutine.status(co))"+
                "coroutine.resume(co)"+
                "coroutine.resume(co)"+
                "coroutine.resume(co)"+
                        "print(coroutine.status(co))"+
                "coroutine.resume(co)";

        globals.load(script).call();
    }
}
