---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by Administrator.
--- DateTime: 2024/12/7 上午 02:47
---

---@class JavaClass
---@field name string
local JavaClass = {}

---@param className string
---@return JavaClass
function JavaClass.bindClass(className)
    local javaClass = luajava.bindClass(className)
    javaClass.name = className
    return javaClass
end

---@param class JavaClass
function JavaClass:New(...)
    return luajava.new(self,...)
end

---@return string
function JavaClass:getName()
    return self.name
end