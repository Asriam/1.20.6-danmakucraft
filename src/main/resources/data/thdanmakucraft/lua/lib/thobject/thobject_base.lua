---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by Administrator.
--- DateTime: 2025/1/27 下午 08:26
---

---@type Class|THObject
THObjectBase = core.defineClass("THObject_Base")
local object = THObjectBase
object.thobject_type = core.thobject_types.thobject
---此函數會在THObject創建時調用
function object:onConstruct()
end

---此函數會在THObject第一次添加到THObjectContainer時調用
---@param position util.Vec3
function object:onInit(position)
    self:initPosition(position)
end

---此函數會在註冊Tasks時被調用
---@param taskManager TaskManager
function object:onRegisterTasks(taskManager)

end

---此函數會在THObject每次tick時調用
function object:onTick()
end

---此函數會在THObject撞擊物體時調用
function object:onHit()
end

---此函數會在THObject死亡時調用
function object:onDead()
end

---此函數會在THObject移除時調用
function object:onRemove()
end

---@param className string
---@return Class|THObject
util.defineTHObject = function(className)
    return core.defineClass(className,THObjectBase)
end