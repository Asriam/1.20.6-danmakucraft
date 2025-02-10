---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by Administrator.
--- DateTime: 2024/12/7 下午 10:27
---

---@class core
---@field mod_id string
local c = {}
core = c

---@param filePath string
function c.doFile(filePath) end

---@param object any
---@return boolean
function c.isValid(object) end

---@param msg string
function c.info(msg) end

---@param msg string
function c.warn(msg) end

---@param x number
---@param y number
---@return core.Vec2
function c.vec2(x, y) end

---@param x number
---@param y number
---@param z number
---@return util.Vec3
function c.vec3(x, y, z) end

---@param className string
---@param parentClass Class
---@return Class
---@overload fun(className:string, superClass:Class)
---@overload fun(className:string, superClass:string)
---@overload fun()
---@overload fun(superClass:Class)
function c.defineClass(className, superClass) end

---@param className self
---@return Class
function c.getClass(className) end